import { query } from '../../../lib/db';
import { getAuthenticatedUser } from '../../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity || !identity.worker_id) {
    return res.status(403).json({ message: 'Forbidden. Worker privileges required.' });
  }

  const { booking_id, status } = req.body;
  if (!booking_id || !status) {
    return res.status(400).json({ message: 'Missing booking_id or status' });
  }

  const validStatuses = ['pending', 'accepted', 'rejected', 'completed'];
  if (!validStatuses.includes(status)) {
    return res.status(400).json({ message: 'Invalid status value' });
  }

  try {
    // Audit check: Verify booking belongs to this worker
    const bookingCheck = await query(
      'SELECT * FROM bookings WHERE booking_id = ? AND worker_id = ?',
      [booking_id, identity.worker_id]
    );

    if (bookingCheck.length === 0) {
      return res.status(404).json({ message: 'Booking not found or not assigned to you' });
    }

    await query('UPDATE bookings SET status = ? WHERE booking_id = ?', [status, booking_id]);
    return res.status(200).json({ message: `Booking status updated to ${status}` });
  } catch (err) {
    console.error('Update booking status error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
