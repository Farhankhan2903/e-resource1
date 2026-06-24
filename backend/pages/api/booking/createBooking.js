import { query } from '../../../lib/db';
import { getAuthenticatedUser } from '../../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity) {
    return res.status(401).json({ message: 'Unauthorized' });
  }

  const { worker_id, appliance, problem, scheduled_at, notes } = req.body;

  if (!worker_id || !appliance || !problem || !scheduled_at) {
    return res.status(400).json({ message: 'Missing booking fields: worker_id, appliance, problem, scheduled_at' });
  }

  try {
    await query(
      'INSERT INTO bookings (user_id, worker_id, appliance, problem, scheduled_at, status, notes, created_at) VALUES (?, ?, ?, ?, ?, "pending", ?, NOW())',
      [identity.user_id, worker_id, appliance, problem, scheduled_at, notes || '']
    );
    return res.status(201).json({ message: 'Booking created successfully' });
  } catch (err) {
    console.error('Create booking error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
