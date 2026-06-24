import { query } from '../../../lib/db';

export default async function handler(req, res) {
  if (req.method !== 'GET') return res.status(405).json({ message: 'Method Not Allowed' });

  try {
    const shops = await query('SELECT * FROM shops');
    // In real app, we'd use lat/lng to sort by distance
    return res.status(200).json(shops);
  } catch (err) {
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
