import { query } from '../../../lib/db';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  try {
    const resources = await query('SELECT * FROM resources');
    return res.status(200).json(resources);
  } catch (err) {
    console.error('Fetch avail resources error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
