import { query, getMemoryDb } from '../../../lib/db';
import { getAuthenticatedUser } from '../../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity || identity.role !== 'admin') {
     return res.status(403).json({ message: 'Forbidden. Admin account required.' });
  }

  try {
    const sql = `
      SELECT l.*, r.name as resource_name, r.price_per_hour, w.shop_name
      FROM allocation_log l
      JOIN resources r ON r.resource_id = l.resource_id
      JOIN worker w ON w.worker_id = l.worker_id
      ORDER BY l.alloc_time DESC
    `;
    let logs = await query(sql);

    if (logs.length === 0) {
      // Memory DB fallback check
      const db = getMemoryDb();
      logs = db.allocation_log.map(l => {
        const r = db.resources.find(res => res.resource_id === l.resource_id);
        const w = db.worker.find(wrk => wrk.worker_id === l.worker_id);
        return {
          ...l,
          resource_name: r ? r.name : 'Unknown Tool',
          price_per_hour: r ? r.price_per_hour : 0.00,
          shop_name: w ? w.shop_name : 'Technician'
        };
      });
    }

    return res.status(200).json(logs);
  } catch (err) {
    console.error('Fetch allocation logs error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
