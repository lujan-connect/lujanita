const fs = require('fs/promises');
const path = require('path');

async function validateFixtures() {
  const base = path.resolve(__dirname, '../apps/dashboard/public/mocks');
  const files = ['users.json', 'services.json', 'serviceSubscriptions.json', 'invoices.json'];
  const data = {
    'users.json': [],
    'services.json': [],
    'serviceSubscriptions.json': [],
    'invoices.json': []
  };

  // Verificar existencia del directorio antes de continuar
  try {
    await fs.access(base);
  } catch (e) {
    throw new Error(`Mocks directory not found: ${base}`);
  }

  for (const file of files) {
    const full = path.join(base, file);
    let raw;
    try {
      raw = await fs.readFile(full, 'utf-8');
    } catch (e) {
      throw new Error(`Missing required fixture file: ${file}`);
    }
    let content;
    try {
      content = JSON.parse(raw);
    } catch (e) {
      throw new Error(`Invalid JSON in ${file}: ${e && e.message ? e.message : String(e)}`);
    }
    if (!Array.isArray(content)) {
      throw new Error(`${file} must contain an array`);
    }
    const ids = new Set();
    for (const item of content) {
      if (typeof item.id !== 'string') {
        throw new Error(`${file} entry missing id`);
      }
      if (ids.has(item.id)) {
        throw new Error(`${file} contains duplicate id ${item.id}`);
      }
      ids.add(item.id);
    }
    data[file] = content;
  }

  const userIds = new Set(data['users.json'].map(u => u.id));
  const serviceIds = new Set(data['services.json'].map(s => s.id));

  for (const sub of data['serviceSubscriptions.json']) {
    if (typeof sub.userId !== 'string' || typeof sub.serviceId !== 'string') {
      throw new Error(`serviceSubscription ${sub.id} missing userId/serviceId string fields`);
    }
    if (!userIds.has(sub.userId)) {
      throw new Error(`serviceSubscription ${sub.id} references missing user ${sub.userId}`);
    }
    if (!serviceIds.has(sub.serviceId)) {
      throw new Error(`serviceSubscription ${sub.id} references missing service ${sub.serviceId}`);
    }
  }

  for (const inv of data['invoices.json']) {
    if (typeof inv.userId !== 'string') {
      throw new Error(`invoice ${inv.id} missing userId field`);
    }
    if (!userIds.has(inv.userId)) {
      throw new Error(`invoice ${inv.id} references missing user ${inv.userId}`);
    }
  }

  return {
    counts: Object.fromEntries(Object.entries(data).map(([k,v]) => [k, v.length]))
  };
}

module.exports = { validateFixtures };

if (require.main === module) {
  validateFixtures()
    .then((summary) => console.log('Fixture validation summary:', summary))
    .catch((err) => {
      console.error('[Fixture Validation Error]', err.message);
      process.exit(1);
    });
}
