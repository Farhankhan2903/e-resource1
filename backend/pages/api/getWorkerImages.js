import { query, getMemoryDb } from '../../lib/db';

export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const { worker_id } = req.query;
  if (!worker_id) {
    return res.status(400).json({ message: 'Missing worker_id query parameter' });
  }

  try {
    const images = await query('SELECT * FROM images WHERE worker_id = ? LIMIT 1', [worker_id]);
    
    if (images.length > 0 && images[0].data) {
      const img = images[0];
      res.setHeader('Content-Type', img.mimetype || 'image/png');
      return res.send(img.data);
    }

    // Default high-quality SVG avatar fallback
    const svgFallback = `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200" width="200" height="200">
        <rect width="100%" height="100%" fill="#121A1C"/>
        <circle cx="100" cy="80" r="40" fill="#1E88E5"/>
        <path d="M40 160 C40 120, 160 120, 160 160" fill="#00BCD4" stroke="#121A1C" stroke-width="5"/>
        <text x="50%" y="185" dominant-baseline="middle" text-anchor="middle" fill="#F4F6F9" font-family="sans-serif" font-weight="bold" font-size="12">KYC DOC (WORKER #${worker_id})</text>
      </svg>
    `;

    res.setHeader('Content-Type', 'image/svg+xml');
    return res.send(svgFallback);
  } catch (err) {
    console.error('Get worker image error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
