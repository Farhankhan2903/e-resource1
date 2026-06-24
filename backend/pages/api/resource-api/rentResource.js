import { query } from '../../../lib/db';
import { getAuthenticatedUser } from '../../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity || (!identity.worker_id && !identity.shop_id)) {
    return res.status(403).json({ message: 'Forbidden. Worker or Shop privileges required.' });
  }

  const { resource_id, alloc_hour } = req.body;
  const actor_id = identity.worker_id || identity.shop_id;

  if (!resource_id || !alloc_hour) {
    return res.status(400).json({ message: 'Missing resource_id or alloc_hour' });
  }

  try {
    // Check availability
    const resourceResult = await query('SELECT * FROM resources WHERE resource_id = ?', [resource_id]);
    if (resourceResult.length === 0) {
      return res.status(404).json({ message: 'Tool resource not found' });
    }

    const tool = resourceResult[0];
    if (tool.avail <= 0) {
      return res.status(400).json({ message: 'This tool is currently out of stock.' });
    }

    // Process rental
    await query('UPDATE resources SET avail = avail - 1 WHERE resource_id = ?', [resource_id]);
    await query(
      'INSERT INTO allocation_log (resource_id, worker_id, alloc_hour, alloc_time, returned) VALUES (?, ?, ?, NOW(), 0)',
      [resource_id, actor_id, alloc_hour]
    );

    return res.status(200).json({ message: 'Tool rented successfully' });
  } catch (err) {
    console.error('Rent resource error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
