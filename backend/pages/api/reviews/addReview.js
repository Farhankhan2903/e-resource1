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

  const { worker_id, booking_id, rating, comment } = req.body;

  if (!worker_id || !booking_id || !rating) {
    return res.status(400).json({ message: 'Missing fields: worker_id, booking_id, rating' });
  }

  const rVal = parseInt(rating);
  if (isNaN(rVal) || rVal < 1 || rVal > 5) {
    return res.status(400).json({ message: 'Rating must be an integer between 1 and 5' });
  }

  try {
    // Optional check: ensure booking is completed and belongs to this user
    const bookingResult = await query(
      'SELECT * FROM bookings WHERE booking_id = ? AND user_id = ?',
      [booking_id, identity.user_id]
    );

    if (bookingResult.length === 0) {
      return res.status(404).json({ message: 'Booking not found or does not belong to you' });
    }

    // Insert review
    await query(
      'INSERT INTO reviews (user_id, worker_id, booking_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?, NOW())',
      [identity.user_id, worker_id, booking_id, rVal, comment || '']
    );

    return res.status(201).json({ message: 'Review submitted successfully!' });
  } catch (err) {
    console.error('Submit review error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
