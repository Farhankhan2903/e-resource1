import { query } from '../../lib/db';
import { signToken } from '../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const { email_id, password } = req.body;

  if (!email_id || !password) {
    return res.status(400).json({ message: 'Email ID and password are required' });
  }

  try {
    const users = await query('SELECT * FROM user WHERE email_id = ?', [email_id]);
    if (!users || users.length === 0) {
      return res.status(401).json({ message: 'Invalid credentials. User not found.' });
    }

    const user = users[0];
    if (user.password !== password) {
      return res.status(401).json({ message: 'Invalid credentials. Incorrect password.' });
    }

    // Determine Role
    let role = 'user';
    if (user.admin_id) {
      role = 'admin';
    } else if (user.worker_id) {
      role = 'worker';
    } else if (user.shop_id) {
      role = 'shop';
    }

    const token = signToken({
      user_id: user.user_id,
      email_id: user.email_id,
      username: user.username,
      role: role,
      worker_id: user.worker_id,
      admin_id: user.admin_id,
      shop_id: user.shop_id
    });

    return res.status(200).json({
      token,
      user_id: user.user_id,
      username: user.username,
      email_id: user.email_id,
      role,
      worker_id: user.worker_id,
      admin_id: user.admin_id,
      shop_id: user.shop_id,
      message: 'Logged in successfully'
    });
  } catch (err) {
    console.error('Login API error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
