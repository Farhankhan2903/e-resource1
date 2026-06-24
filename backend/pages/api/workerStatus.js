import { query } from '../../lib/db';
import { getAuthenticatedUser } from '../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity) {
    return res.status(401).json({ message: 'Unauthorized' });
  }

  try {
    const userResult = await query('SELECT * FROM user WHERE user_id = ?', [identity.user_id]);
    if (!userResult || userResult.length === 0) {
      return res.status(404).json({ message: 'User not found' });
    }

    const user = userResult[0];
    if (!user.worker_id) {
       return res.status(200).json({ has_applied: false, verified: null });
    }

    const workerResult = await query('SELECT * FROM worker WHERE worker_id = ?', [user.worker_id]);
    if (!workerResult || workerResult.length === 0) {
      return res.status(200).json({ has_applied: false, verified: null }); // Treat as not applied if record missing
    }

    const worker = workerResult[0];
    return res.status(200).json({
      has_applied: true,
      worker_id: worker.worker_id,
      shop_name: worker.shop_name,
      type: worker.type,
      verified: worker.verified // null = pending, 1 = verified, 0 = rejected
    });
  } catch (err) {
    console.error('Worker status API error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
