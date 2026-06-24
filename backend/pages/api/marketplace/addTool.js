import { query } from '../../../lib/db';
import { getAuthenticatedUser } from '../../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'POST') return res.status(405).json({ message: 'Method Not Allowed' });

  const identity = getAuthenticatedUser(req);
  if (!identity || identity.role !== 'shop') return res.status(403).json({ message: 'Forbidden' });

  const { shop_id, name, category, description, price_hr, price_day, qty } = req.body;

  try {
    await query(
      'INSERT INTO tools (shop_id, name, category, description, price_hr, price_day, qty) VALUES (?, ?, ?, ?, ?, ?, ?)',
      [shop_id, name, category, description, price_hr, price_day, qty]
    );
    return res.status(201).json({ message: 'Tool added successfully' });
  } catch (err) {
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
