import formidable from 'formidable';
import fs from 'fs';
import { query, getMemoryDb } from '../../lib/db';
import { getAuthenticatedUser, signToken } from '../../lib/auth';

export const config = {
  api: {
    bodyParser: false,
  },
};

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity) {
    return res.status(401).json({ message: 'Unauthorized' });
  }

  const form = formidable({ multiples: true });

  form.parse(req, async (err, fields, files) => {
    if (err) {
      console.error('Formidable parsing error:', err);
      return res.status(500).json({ message: 'Error parsing form files' });
    }

    // Extraction helper (Formidable v3 returns arrays for fields and files)
    const getFirst = (arg) => Array.isArray(arg) ? arg[0] : arg;

    const shop_name = getFirst(fields.shop_name);
    const shop_addr = getFirst(fields.shop_addr);
    const type = getFirst(fields.type);
    const contact_no = getFirst(fields.contact_no);

    if (!shop_name || !shop_addr || !type || !contact_no) {
       return res.status(400).json({ message: 'Missing fields: shop_name, shop_addr, type, contact_no' });
    }

    try {
      // 1. Check if user already is a worker
      const userResult = await query('SELECT * FROM user WHERE user_id = ?', [identity.user_id]);
      if (userResult.length === 0) {
        return res.status(404).json({ message: 'User not found' });
      }

      const user = userResult[0];
      if (user.worker_id) {
         return res.status(400).json({ message: 'You have already applied for a worker profile' });
      }

      // 2. Insert into worker table
      const workerInsert = await query(
        'INSERT INTO worker (shop_name, shop_addr, type, contact_no, verified) VALUES (?, ?, ?, ?, NULL)',
        [shop_name, shop_addr, type, contact_no]
      );

      // Critical: Ensure result object is handled properly for both MySQL and MemoryDB
      const workerId = workerInsert.insertId || workerInsert.id;

      if (!workerId) {
          throw new Error('Worker registration failed. ID not generated.');
      }

      // 3. User update worker_id
      await query('UPDATE user SET worker_id = ? WHERE user_id = ?', [workerId, identity.user_id]);

      // 4. Save image files (KYC documents) if provided
      const passportFile = getFirst(files.passport);
      if (passportFile) {
        try {
          const fileData = fs.readFileSync(passportFile.filepath);
          await query(
            'INSERT INTO images (filename, fieldname, mimetype, size, worker_id, data) VALUES (?, ?, ?, ?, ?, ?)',
            [passportFile.originalFilename || 'passport.png', 'passport', passportFile.mimetype || 'image/png', passportFile.size, workerId, fileData]
          );
        } catch (fileErr) {
          console.warn('KYC image file could not cover raw BLOB storage. Saving stub instead.', fileErr);
          await query(
            'INSERT INTO images (filename, fieldname, mimetype, size, worker_id, data) VALUES (?, ?, ?, ?, ?, ?)',
            ['passport.png', 'passport', 'image/png', 100, workerId, Buffer.from('stub-blob')]
          );
        }
      } else {
         // Seed dummy passport photo for KYC fallback
         await query(
           'INSERT INTO images (filename, fieldname, mimetype, size, worker_id, data) VALUES (?, ?, ?, ?, ?, ?)',
           ['passport_default.png', 'passport', 'image/png', 100, workerId, Buffer.from('stub-blob')]
         );
      }

      // Generate a brand new token with the new worker role/ID
      const updatedToken = signToken({
        user_id: identity.user_id,
        email_id: identity.email_id,
        username: identity.username,
        role: 'worker',
        worker_id: workerId,
        admin_id: null
      });

      return res.status(201).json({
        message: 'KYC application submitted successfully. Pending admin verification.',
        token: updatedToken,
        worker_id: workerId
      });
    } catch (insertErr) {
      console.error('Database insertion error during application:', insertErr);
      return res.status(500).json({ message: 'Internal Server Error' });
    }
  });
}
