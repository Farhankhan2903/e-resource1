import { query, getMemoryDb } from '../../../lib/db';
import { getAuthenticatedUser } from '../../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity || (!identity.worker_id && !identity.shop_id)) {
    return res.status(403).json({ message: 'Forbidden. Worker or Shop privileges required.' });
  }

  const actor_id = identity.worker_id || identity.shop_id;

  try {
    const sql = `
      SELECT l.*, r.name as resource_name, r.price_per_hour
      FROM allocation_log l
      JOIN resources r ON r.resource_id = l.resource_id
      WHERE l.worker_id = ? AND l.returned = 0
    `;
    let items = await query(sql, [actor_id]);

    if (items.length === 0) {
      // Memory DB fallback check
      const db = getMemoryDb();
      items = db.allocation_log
        .filter(l => l.worker_id === actor_id && l.returned === 0)
        .map(l => {
          const r = db.resources.find(res => res.resource_id === l.resource_id);
          return {
            ...l,
            resource_name: r ? r.name : 'Unknown Tool',
            price_per_hour: r ? r.price_per_hour : 0.00
          };
        });
    }

    return res.status(200).json(items);
  } catch (err) {
    console.error('Fetch rented resources error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
