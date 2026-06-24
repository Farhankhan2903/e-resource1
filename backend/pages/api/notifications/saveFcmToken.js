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

  const { token } = req.body;
  if (!token) {
    return res.status(400).json({ message: 'Missing FCM token' });
  }

  try {
    await query(
      'INSERT INTO fcm_tokens (user_id, token, updated_at) VALUES (?, ?, NOW()) ON DUPLICATE KEY UPDATE updated_at = NOW()',
      [identity.user_id, token]
    );

    return res.status(200).json({ message: 'FCM token updated successfully' });
  } catch (err) {
    console.error('Save FCM token error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
