import { query, getMemoryDb } from '../../../lib/db';
import { getAuthenticatedUser } from '../../../lib/auth';

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const identity = getAuthenticatedUser(req);
  if (!identity || (!identity.worker_id && !identity.shop_id)) {
    return res.status(403).json({ message: 'Forbidden. Worker or Shop privileges required.' });
  }

  const { allocation_id } = req.body;
  const actor_id = identity.worker_id || identity.shop_id;
  if (!allocation_id) {
    return res.status(400).json({ message: 'Missing allocation_id' });
  }

  try {
    const logResult = await query('SELECT * FROM allocation_log WHERE allocation_id = ?', [allocation_id]);
    if (logResult.length === 0) {
      return res.status(404).json({ message: 'Allocation log not found' });
    }

    const log = logResult[0];
    if (log.returned === 1) {
      return res.status(400).json({ message: 'This tool is already marked as returned' });
    }

    const toolResult = await query('SELECT * FROM resources WHERE resource_id = ?', [log.resource_id]);
    if (toolResult.length === 0) {
      return res.status(404).json({ message: 'Associated tool not found' });
    }

    const tool = toolResult[0];

    // Return tool to stock
    await query('UPDATE resources SET avail = avail + 1 WHERE resource_id = ?', [log.resource_id]);
    await query('UPDATE allocation_log SET returned = 1 WHERE allocation_id = ?', [allocation_id]);

    const billingAmount = log.alloc_hour * tool.price_per_hour;

    return res.status(200).json({
      message: 'Tool returned successfully',
      billing_hours: log.alloc_hour,
      rate_per_hour: tool.price_per_hour,
      total_amount: billingAmount
    });
  } catch (err) {
    console.error('Return resource error:', err);
    return res.status(500).json({ message: 'Internal Server Error' });
  }
}
