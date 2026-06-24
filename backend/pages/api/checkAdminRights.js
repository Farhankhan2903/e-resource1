import { getAuthenticatedUser } from '../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity || identity.role !== 'admin') {
    return res.status(403).json({ is_admin: false, message: 'Forbidden. Admin account required.' });
  }

  return res.status(200).json({
    is_admin: true,
    user_id: identity.user_id,
    username: identity.username,
    email_id: identity.email_id
  });
}
