import { query, getMemoryDb } from '../../../lib/db';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const { worker_id } = req.query;

  try {
    let reviews = [];
    if (worker_id) {
      const sql_worker = `
        SELECT r.*, u.username as customer_name
        FROM reviews r
        JOIN user u ON u.user_id = r.user_id
        WHERE r.worker_id = ?
        ORDER BY r.created_at DESC
      `;
      reviews = await query(sql_worker, [worker_id]);

      if (reviews.length === 0) {
        // Fallback
        const db = getMemoryDb();
        reviews = db.reviews
          .filter(r => r.worker_id === parseInt(worker_id))
          .map(r => {
             const u = db.user.find(usr => usr.user_id === r.user_id);
             return {
               ...r,
               customer_name: u ? u.username : 'Customer'
             };
          });
      }
    } else {
      const sql_all = `
        SELECT r.*, u.username as customer_name, w.shop_name
        FROM reviews r
        JOIN user u ON u.user_id = r.user_id
        JOIN worker w ON w.worker_id = r.worker_id
        ORDER BY r.created_at DESC
      `;
      reviews = await query(sql_all);

      if (reviews.length === 0) {
        // Fallback
        const db = getMemoryDb();
        reviews = db.reviews.map(r => {
          const u = db.user.find(usr => usr.user_id === r.user_id);
          const w = db.worker.find(wrk => wrk.worker_id === r.worker_id);
          return {
            ...r,
            customer_name: u ? u.username : 'Customer',
            shop_name: w ? w.shop_name : 'Technician Hub'
          };
        });
      }
    }

    return res.status(200).json(reviews);
  } catch (err) {
    console.error('Fetch reviews error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
