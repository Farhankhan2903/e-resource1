import { query } from '../../../lib/db';
import { getAuthenticatedUser } from '../../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity) {
    return res.status(401).json({ message: 'Unauthorized' });
  }

  const { booking_id } = req.query;
  if (!booking_id) {
    return res.status(400).json({ message: 'Missing booking_id query parameter' });
  }

  try {
    // Audit check: Verify user is either the customer or the worker in this booking
    const bookingResult = await query('SELECT * FROM bookings WHERE booking_id = ?', [booking_id]);
    if (bookingResult.length === 0) {
      return res.status(404).json({ message: 'Booking not found' });
    }

    const booking = bookingResult[0];
    const isCustomer = booking.user_id === identity.user_id;
    const isWorker = booking.worker_id === identity.worker_id;

    if (!isCustomer && !isWorker) {
      return res.status(403).json({ message: 'Forbidden' });
    }

    const messages = await query(
      'SELECT * FROM chat_messages WHERE booking_id = ? ORDER BY sent_at ASC',
      [booking_id]
    );

    return res.status(200).json(messages);
  } catch (err) {
    console.error('Fetch messages error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
