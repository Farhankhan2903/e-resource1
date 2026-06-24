import { query, getMemoryDb } from '../../../lib/db';
import { getAuthenticatedUser } from '../../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity) {
    return res.status(401).json({ message: 'Unauthorized' });
  }

  try {
    let bookings = [];

    if (identity.role === 'worker' && identity.worker_id) {
      // Query bookings assigned to this worker, retrieve customer name
      const sql_worker = `
        SELECT b.*, u.username as customer_name, u.email_id as customer_email
        FROM bookings b
        JOIN user u ON u.user_id = b.user_id
        WHERE b.worker_id = ?
        ORDER BY b.created_at DESC
      `;
      bookings = await query(sql_worker, [identity.worker_id]);

      if (bookings.length === 0) {
        // Fallback
        const db = getMemoryDb();
        bookings = db.bookings
          .filter(b => b.worker_id === identity.worker_id)
          .map(b => {
             const u = db.user.find(usr => usr.user_id === b.user_id);
             return {
               ...b,
               customer_name: u ? u.username : 'Customer',
               customer_email: u ? u.email_id : 'customer@test.com'
             };
          });
      }
    } else {
      // Query bookings booked by this regular user, retrieve worker shop name and type
      const sql_user = `
        SELECT b.*, w.shop_name, w.type as worker_type, w.contact_no as worker_contact
        FROM bookings b
        JOIN worker w ON w.worker_id = b.worker_id
        WHERE b.user_id = ?
        ORDER BY b.created_at DESC
      `;
      bookings = await query(sql_user, [identity.user_id]);

      if (bookings.length === 0) {
        // Fallback
        const db = getMemoryDb();
        bookings = db.bookings
          .filter(b => b.user_id === identity.user_id)
          .map(b => {
             const w = db.worker.find(wrk => wrk.worker_id === b.worker_id);
             return {
               ...b,
               shop_name: w ? w.shop_name : 'Technician Hub',
               worker_type: w ? w.type : 'Electrician',
               worker_contact: w ? w.contact_no : '+15551234'
             };
          });
      }
    }

    return res.status(200).json(bookings);
  } catch (err) {
    console.error('Get bookings error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
