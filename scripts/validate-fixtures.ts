import fs from "fs/promises";
import path from "path";

export async function validateFixtures(): Promise<void> {
  const base = path.resolve(__dirname, "../apps/dashboard/public/mocks");
  const files = [
    "users.json",
    "services.json",
    "serviceSubscriptions.json",
    "invoices.json",
  ] as const;
  const data: Record<(typeof files)[number], any[]> = {
    "users.json": [],
    "services.json": [],
    "serviceSubscriptions.json": [],
    "invoices.json": [],
  };

  for (const file of files) {
    const full = path.join(base, file);
    const content = JSON.parse(await fs.readFile(full, "utf-8"));
    if (!Array.isArray(content)) {
      throw new Error(`${file} must contain an array`);
    }
    const ids = new Set<string>();
    for (const item of content) {
      if (typeof item.id !== "string") {
        throw new Error(`${file} entry missing id`);
      }
      if (ids.has(item.id)) {
        throw new Error(`${file} contains duplicate id ${item.id}`);
      }
      ids.add(item.id);
    }
    data[file] = content;
  }

  const userIds = new Set(data["users.json"].map((u) => u.id));
  const serviceIds = new Set(data["services.json"].map((s) => s.id));
  for (const sub of data["serviceSubscriptions.json"]) {
    if (!userIds.has(sub.userId)) {
      throw new Error(
        `serviceSubscription ${sub.id} references missing user ${sub.userId}`,
      );
    }
    if (!serviceIds.has(sub.serviceId)) {
      throw new Error(
        `serviceSubscription ${sub.id} references missing service ${sub.serviceId}`,
      );
    }
  }
  for (const inv of data["invoices.json"]) {
    if (!userIds.has(inv.userId)) {
      throw new Error(
        `invoice ${inv.id} references missing user ${inv.userId}`,
      );
    }
  }
}

if (require.main === module) {
  validateFixtures()
    .then(() => console.log("All fixture validations passed."))
    .catch((err) => {
      console.error(err);
      process.exit(1);
    });
}

export default validateFixtures;
