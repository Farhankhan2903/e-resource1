// Database connection helper with a persistent in-memory fallback for immediate run reliability.
import mysql from 'mysql2/promise';
import dotenv from 'dotenv';
import path from 'path';

// Force load .env from the backend root
dotenv.config({ path: path.resolve(process.cwd(), '.env') });

let pool = null;
let useMemoryFallback = false;

// Persistent In-Memory database tables state representing MySQL fallback
const memoryDb = {
  admin: [
    { admin_id: 1, can_verify: 1, can_remove: 1, can_access_db: 1 }
  ],
  user: [
    { user_id: 1, username: 'admin', email_id: 'admin@eresource.com', password: 'admin123', worker_id: null, admin_id: 1, shop_id: null },
    { user_id: 2, username: 'jane_customer', email_id: 'jane@gmail.com', password: 'password123', worker_id: null, admin_id: null, shop_id: null },
    { user_id: 3, username: 'elite_repair', email_id: 'elite@gmail.com', password: 'password123', worker_id: 1, admin_id: null, shop_id: null },
    { user_id: 4, username: 'Admin Legacy', email_id: 'admin@admin.com', password: 'admin1001', worker_id: null, admin_id: 1, shop_id: null },
    { user_id: 5, username: 'Main Street Tools', email_id: 'shop@test.com', password: 'password123', worker_id: null, admin_id: null, shop_id: 1 }
  ],
  worker: [
    { worker_id: 1, shop_name: 'ElectroTech Systems', shop_addr: '123 Avenue, Tech District', type: 'Electrician', contact_no: '+15550199', verified: 1 },
    { worker_id: 2, shop_name: 'CompRepair Hub', shop_addr: '456 Lane, Silicon Valley', type: 'Computer', contact_no: '+15550299', verified: null } // pending
  ],
  images: [
    { id: 1, filename: 'kyc_sample.png', fieldname: 'passport', mimetype: 'image/png', size: 1024, worker_id: 2, data: Buffer.from('fake-data') }
  ],
  resources: [
    { resource_id: 1, name: 'Industrial Multimeter', price_per_hour: 199.00, avail: 5, total: 5, description: 'High precision digital multimeter for electric troubleshooting.' },
    { resource_id: 2, name: 'Heavy-Duty Solder Station', price_per_hour: 299.00, avail: 3, total: 3, description: 'Temperature controlled soldering iron for computer chips.' },
    { resource_id: 3, name: 'Portable Appliance Tester', price_per_hour: 249.00, avail: 4, total: 4, description: 'Handy tool to test insulation resistance and leakage safety.' },
    { resource_id: 4, name: 'Demolition Electric Hammer', price_per_hour: 499.00, avail: 2, total: 2, description: 'High power percussion hammer for concrete wall wiring channels.' }
  ],
  allocation_log: [
    { allocation_id: 1, resource_id: 1, worker_id: 1, alloc_hour: 2, alloc_time: new Date().toISOString(), returned: 0 }
  ],
  bookings: [
    { booking_id: 1, user_id: 2, worker_id: 1, appliance: 'Washing Machine', problem: 'Leaks water while spinning cycle', scheduled_at: '2026-05-25 14:00', status: 'pending', notes: 'Please ring back bell', created_at: new Date().toISOString() }
  ],
  reviews: [
    { review_id: 1, user_id: 2, worker_id: 1, booking_id: 1, rating: 5, comment: 'Excellent prompt service', created_at: new Date().toISOString() }
  ],
  chat_messages: [
    { message_id: 1, booking_id: 1, sender_id: 2, message: 'Hello, could you come exactly on time?', sent_at: new Date().toISOString() },
    { message_id: 2, booking_id: 1, sender_id: 3, message: 'Of course! I will head out around 1:30 PM.', sent_at: new Date().toISOString() }
  ],
  fcm_tokens: [],
  // Marketplace Tables
  shops: [
    { shop_id: 1, owner_id: 5, name: 'Main Street Tools', description: 'Premium power tools for rent', address: 'MG Road, Bangalore', lat: 12.9716, lng: 77.5946, contact: '+91 9876543210', hours: '9 AM - 8 PM', rating: 4.5 }
  ],
  tools: [
    { tool_id: 1, shop_id: 1, name: 'Bosch Hammer Drill', category: 'Power Tools', description: '800W Professional Drill', price_hr: 50, price_day: 400, qty: 3, status: 'Available' },
    { tool_id: 2, shop_id: 1, name: 'Makita Angle Grinder', category: 'Power Tools', description: '4-inch Grinder', price_hr: 30, price_day: 250, qty: 2, status: 'Available' }
  ],
  rental_requests: []
};

// Auto-increment counters for fallback
const counters = {
  admin: 1,
  user: 4,
  worker: 2,
  images: 1,
  resources: 4,
  allocation_log: 1,
  bookings: 1,
  reviews: 1,
  chat_messages: 2,
  fcm_tokens: 0
};

// Try initializing MySQL pool
try {
  const host = process.env.DB_HOST || 'localhost';
  const user = process.env.DB_USER || 'root';
  const password = process.env.DB_PASSWORD || process.env.DB_PASS || 'Root@123456';
  const database = process.env.DB_NAME || 'e_resource_solution';

  pool = mysql.createPool({
    host,
    user,
    password,
    database,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
  });
  console.log('MySQL connection pool initialized.');
} catch (err) {
  console.warn('MySQL configuration failed, using in-memory database fallback instead.', err);
  useMemoryFallback = true;
}

export async function query(sql, params = []) {
  const cleaned = sql.replace(/\s+/g, ' ').trim().toLowerCase();

  if (useMemoryFallback) {
    const rows = handleMemoryQuery(sql, params);
    console.log(`[Memory DB] SQL: ${cleaned} | Rows: ${rows.length}`);
    return rows;
  }

  try {
    const [rows] = await pool.query(sql, params);
    return rows;
  } catch (err) {
    console.error(`[Database Error] SQL: ${sql} | Error: ${err.message}`);
    useMemoryFallback = true;
    const rows = handleMemoryQuery(sql, params);
    console.log(`[Fallback] SQL: ${cleaned} | Rows: ${rows.length}`);
    return rows;
  }
}

function handleMemoryQuery(sql, params) {
  const cleaned = sql.replace(/\s+/g, ' ').trim().toLowerCase();

  // Helper to extract value from WHERE clause if params are not used
  const extractVal = (key) => {
      const parts = cleaned.split(key + ' =');
      if (parts.length > 1) {
          return parts[1].trim().split(' ')[0].replace(/['?]/g, '');
      }
      return null;
  };

  // 1. USER QUERIES
  if (cleaned.includes('from user')) {
      if (cleaned.includes('email_id =')) {
          const email = params[0] || extractVal('email_id');
          const user = memoryDb.user.find(u => u.email_id?.toLowerCase() === email?.toLowerCase());
          return user ? [user] : [];
      }
      if (cleaned.includes('user_id =')) {
          const id = parseInt(params[0]) || parseInt(extractVal('user_id'));
          const user = memoryDb.user.find(u => u.user_id === id);
          return user ? [user] : [];
      }
      if (cleaned.includes('worker_id =')) {
          const id = parseInt(params[0]) || parseInt(extractVal('worker_id'));
          const user = memoryDb.user.find(u => u.worker_id === id);
          return user ? [user] : [];
      }
  }
  if (cleaned.startsWith('insert into user')) {
    const id = ++counters.user;
    const newUser = { user_id: id, username: params[0], email_id: params[1], password: params[2], worker_id: null, admin_id: null, shop_id: null };
    memoryDb.user.push(newUser);

    // Log for debugging
    console.log(`[Memory DB] User added: ${newUser.email_id}`);

    return { insertId: id, affectedRows: 1, id: id };
  }
  if (cleaned.includes('update user set worker_id =')) {
      const val = params[0];
      const id = params[1];
      const user = memoryDb.user.find(u => u.user_id === parseInt(id));
      if (user) user.worker_id = val;
      return { affectedRows: 1 };
  }

  // 2. WORKER QUERIES
  if (cleaned.includes('from worker')) {
      // Joins for Profile with Ratings
      if (cleaned.includes('join user') && (cleaned.includes('u.user_id =') || cleaned.includes('u.worker_id =') || cleaned.includes('w.worker_id ='))) {
          const id = parseInt(params[0]) || parseInt(extractVal('u.user_id')) || parseInt(extractVal('u.worker_id')) || parseInt(extractVal('w.worker_id'));

          let user = memoryDb.user.find(u => u.user_id === id || u.worker_id === id);
          let worker = memoryDb.worker.find(w => w.worker_id === (user?.worker_id || id));

          if (worker) {
              if (!user) user = memoryDb.user.find(u => u.worker_id === worker.worker_id);
              const revs = memoryDb.reviews.filter(r => r.worker_id === worker.worker_id);
              const total = revs.length;
              const avg = total > 0 ? revs.reduce((sum, r) => sum + r.rating, 0) / total : 0;
              return [{ ...worker, email_id: user?.email_id || '', username: user?.username || 'Worker', avg_rating: avg, total_reviews: total }];
          }
          return [];
      }

      // Simple Selects
      if (cleaned.includes('worker_id =')) {
          const id = parseInt(params[0]) || parseInt(extractVal('worker_id'));
          const w = memoryDb.worker.find(wrk => wrk.worker_id === id);
          return w ? [w] : [];
      }
      if (cleaned.includes('verified is null')) {
          return memoryDb.worker.filter(w => w.verified === null).map(w => {
              const u = memoryDb.user.find(usr => usr.worker_id === w.worker_id);
              return { ...w, username: u?.username || 'Applicant', email_id: u?.email_id || 'test@test.com' };
          });
      }
      if (cleaned.includes('verified = 1')) return memoryDb.worker.filter(w => w.verified === 1);
      if (!cleaned.includes('where')) return memoryDb.worker;
  }

  if (cleaned.startsWith('insert into worker')) {
    const id = ++counters.worker;
    memoryDb.worker.push({ worker_id: id, shop_name: params[0], shop_addr: params[1], type: params[2], contact_no: params[3], verified: null });
    return { insertId: id, affectedRows: 1 };
  }
  if (cleaned.includes('update worker set verified =')) {
      let val = null;
      if (cleaned.includes('verified = 1')) val = 1;
      else if (cleaned.includes('verified = 0')) val = 0;
      else val = params[0];

      const id = parseInt(params[params.length - 1]) || parseInt(extractVal('worker_id'));
      const w = memoryDb.worker.find(wrk => wrk.worker_id === id);
      if (w) w.verified = val;
      return { affectedRows: 1 };
  }

  // 3. BOOKING QUERIES
  if (cleaned.includes('from bookings')) {
      if (cleaned.includes('user_id =')) {
          const id = parseInt(params[0]);
          return memoryDb.bookings.filter(b => b.user_id === id).map(b => {
              const w = memoryDb.worker.find(wrk => wrk.worker_id === b.worker_id);
              return { ...b, shop_name: w?.shop_name || 'Technician', type: w?.type || 'Service' };
          });
      }
      if (cleaned.includes('worker_id =')) {
          const id = parseInt(params[0]);
          return memoryDb.bookings.filter(b => b.worker_id === id).map(b => {
              const u = memoryDb.user.find(usr => usr.user_id === b.user_id);
              return { ...b, customer_name: u?.username || 'Customer' };
          });
      }
  }
  if (cleaned.startsWith('insert into bookings')) {
    const id = ++counters.bookings;
    const nb = { booking_id: id, user_id: params[0], worker_id: params[1], appliance: params[2], problem: params[3], scheduled_at: params[4], status: 'pending', notes: params[5], created_at: new Date().toISOString() };
    memoryDb.bookings.push(nb);
    return { insertId: id, affectedRows: 1 };
  }

  // 4. REVIEW QUERIES
  if (cleaned.includes('from reviews')) {
      if (cleaned.includes('worker_id =')) {
          const id = parseInt(params[0]) || parseInt(extractVal('worker_id')) || parseInt(extractVal('r.worker_id'));
          return memoryDb.reviews.filter(r => r.worker_id === id).map(r => {
              const u = memoryDb.user.find(usr => usr.user_id === r.user_id);
              return { ...r, customer_name: u?.username || 'Customer' };
          });
      }
      return memoryDb.reviews.map(r => {
          const u = memoryDb.user.find(usr => usr.user_id === r.user_id);
          const w = memoryDb.worker.find(wrk => wrk.worker_id === r.worker_id);
          return { ...r, customer_name: u?.username || 'Customer', shop_name: w?.shop_name || 'Technician' };
      });
  }

  // 5. RESOURCE/RENTAL QUERIES
  if (cleaned.includes('from resources')) {
      if (cleaned.includes('resource_id =')) {
          const id = parseInt(params[0]) || parseInt(extractVal('resource_id'));
          const r = memoryDb.resources.find(res => res.resource_id === id);
          return r ? [r] : [];
      }
      return memoryDb.resources;
  }
  if (cleaned.includes('from allocation_log')) {
      if (cleaned.includes('worker_id =')) {
          const id = parseInt(params[0]);
          return memoryDb.allocation_log.filter(l => l.worker_id === id).map(l => {
              const r = memoryDb.resources.find(res => res.resource_id === l.resource_id);
              return { ...l, resource_name: r?.name, price_per_hour: r?.price_per_hour };
          });
      }
      return memoryDb.allocation_log.map(l => {
          const r = memoryDb.resources.find(res => res.resource_id === l.resource_id);
          const w = memoryDb.worker.find(wrk => wrk.worker_id === l.worker_id);
          return { ...l, resource_name: r?.name, price_per_hour: r?.price_per_hour, shop_name: w?.shop_name };
      });
  }
  if (cleaned.startsWith('insert into allocation_log')) {
      const id = ++counters.allocation_log;
      memoryDb.allocation_log.push({ allocation_id: id, resource_id: params[0], worker_id: params[1], alloc_hour: params[2], alloc_time: new Date().toISOString(), returned: 0 });
      return { insertId: id, affectedRows: 1 };
  }
  if (cleaned.includes('update resources set avail = avail - 1')) {
      const id = parseInt(params[0]);
      const r = memoryDb.resources.find(res => res.resource_id === id);
      if (r) r.avail = Math.max(0, r.avail - 1);
      return { affectedRows: 1 };
  }
  if (cleaned.includes('update resources set avail = avail + 1')) {
      const id = parseInt(params[0]);
      const r = memoryDb.resources.find(res => res.resource_id === id);
      if (r) r.avail = Math.min(r.total, r.avail + 1);
      return { affectedRows: 1 };
  }
  if (cleaned.includes('update allocation_log set returned = 1')) {
      const id = parseInt(params[0]) || parseInt(extractVal('allocation_id'));
      const l = memoryDb.allocation_log.find(log => log.allocation_id === id);
      if (l) l.returned = 1;
      return { affectedRows: 1 };
  }

  // 6. MARKETPLACE QUERIES
  if (cleaned.includes('from shops where owner_id =')) {
      const id = parseInt(params[0]) || parseInt(extractVal('owner_id'));
      return memoryDb.shops.filter(s => s.owner_id === id);
  }
  if (cleaned.includes('select * from shops')) {
      return memoryDb.shops;
  }
  if (cleaned.startsWith('insert into shops')) {
      const id = memoryDb.shops.length + 1;
      memoryDb.shops.push({ shop_id: id, owner_id: params[0], name: params[1], description: params[2], address: params[3], lat: params[4], lng: params[5], contact: params[6], hours: params[7], rating: 0 });
      return { insertId: id, affectedRows: 1 };
  }
  if (cleaned.includes('from tools where shop_id =')) {
      const id = parseInt(params[0]) || parseInt(extractVal('shop_id'));
      return memoryDb.tools.filter(t => t.shop_id === id);
  }
  if (cleaned.startsWith('insert into tools')) {
      const id = memoryDb.tools.length + 1;
      memoryDb.tools.push({ tool_id: id, shop_id: params[0], name: params[1], category: params[2], description: params[3], price_hr: params[4], price_day: params[5], qty: params[6], status: 'Available' });
      return { insertId: id, affectedRows: 1 };
  }
  if (cleaned.includes('delete from tools where tool_id =')) {
      const id = parseInt(params[0]) || parseInt(extractVal('tool_id'));
      memoryDb.tools = memoryDb.tools.filter(t => t.tool_id !== id);
      return { affectedRows: 1 };
  }
  if (cleaned.startsWith('insert into rental_requests')) {
      const id = memoryDb.rental_requests.length + 1;
      memoryDb.rental_requests.push({ request_id: id, worker_id: params[0], tool_id: params[1], duration_hrs: params[2], status: 'Pending', created_at: new Date().toISOString() });
      return { insertId: id, affectedRows: 1 };
  }

  return [];
}

export function getMemoryDb() { return memoryDb; }
