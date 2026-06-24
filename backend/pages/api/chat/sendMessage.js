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

  const { booking_id, message } = req.body;
  if (!booking_id || !message) {
    return res.status(400).json({ message: 'Missing booking_id or message' });
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
      return res.status(403).json({ message: 'Forbidden. You are not a member of this service conversation.' });
    }

    await query(
      'INSERT INTO chat_messages (booking_id, sender_id, message, sent_at) VALUES (?, ?, ?, NOW())',
      [booking_id, identity.user_id, message]
    );

    return res.status(201).json({ message: 'Message sent successfully' });
  } catch (err) {
    console.error('Send message error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
