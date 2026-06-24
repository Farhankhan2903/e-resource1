import { query } from '../../lib/db';
import { signToken } from '../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const { username, email_id, password } = req.body;

  if (!username || !email_id || !password) {
    return res.status(400).json({ message: 'Missing username, email_id, or password' });
  }

  try {
    const existing = await query('SELECT * FROM user WHERE email_id = ?', [email_id]);
    if (existing.length > 0) {
      return res.status(400).json({ message: 'Email ID already registered' });
    }

    const result = await query(
      'INSERT INTO user (username, email_id, password, worker_id, admin_id, shop_id) VALUES (?, ?, ?, NULL, NULL, NULL)',
      [username, email_id, password]
    );

    const userId = result.insertId || result.id;

    const token = signToken({
      user_id: userId,
      email_id,
      username,
      role: 'user',
      worker_id: null,
      admin_id: null,
      shop_id: null
    });

    return res.status(201).json({
      token,
      user_id: userId,
      username,
      email_id,
      role: 'user',
      message: 'Registered successfully'
    });
  } catch (err) {
    console.error('Registration API error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
