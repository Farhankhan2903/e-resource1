import { query } from '../../lib/db';
import { getAuthenticatedUser } from '../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity || identity.role !== 'admin') {
     return res.status(403).json({ message: 'Forbidden' });
  }

  const { worker_id } = req.body;
  if (!worker_id) {
    return res.status(400).json({ message: 'Missing worker_id' });
  }

  try {
    // Unset worker_id in user first just in case
    await query('UPDATE user SET worker_id = NULL WHERE worker_id = ?', [worker_id]);
    await query('DELETE FROM worker WHERE worker_id = ?', [worker_id]);
    return res.status(200).json({ message: 'Worker profile removed successfully' });
  } catch (err) {
    console.error('Remove worker error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
