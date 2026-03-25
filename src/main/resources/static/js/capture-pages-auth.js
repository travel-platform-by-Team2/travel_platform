const { chromium } = require("playwright");
const fs = require("fs");
const path = require("path");

const baseUrl = process.env.CAPTURE_BASE_URL || "http://localhost:8080";
const loginEmail = process.env.CAPTURE_LOGIN_EMAIL;
const loginPassword = process.env.CAPTURE_LOGIN_PASSWORD;

const pages = [
  { name: "trip-list", path: "/trip" },
  { name: "trip-create", path: "/trip/create" },
  { name: "trip-detail", path: "/trip/detail?id=1" },
  { name: "trip-place-add", path: "/trip/place?id=1" },
  

  { name: "board-list", path: "/boards" },
  { name: "board-new", path: "/boards/new" },
  { name: "board-detail", path: "/boards/1" },
  { name: "board-edit", path: "/boards/1/edit" },
  { name: "board-update", path: "/boards/1/update" },
  { name: "board-delete", path: "/boards/1/delete" },
  { name: "board-replies", path: "/boards/1/replies" },
  { name: "board-reply-delete", path: "/boards/1/replies/1/delete" },

  { name: "calendar", path: "/calendar" },

  { name: "booking-map-detail", path: "/bookings/map-detail" },
  { name: "booking-checkout", path: "/bookings/checkout" },
  { name: "booking-complete", path: "/bookings/complete" },

  { name: "mypage", path: "/mypage" },
  { name: "mypage-bookings", path: "/mypage/bookings" },
  { name: "mypage-booking-detail", path: "/mypage/bookings/1" },
  { name: "mypage-password", path: "/mypage/password" },
  { name: "mypage-withdraw", path: "/mypage/withdraw" },

  { name: "admin-dashboard", path: "/admin" },
  { name: "admin-users", path: "/admin/users" },
  { name: "admin-boards", path: "/admin/boards" }
];

function requireEnv(value, name) {
  if (!value || !value.trim()) {
    throw new Error(`${name} is required`);
  }
  return value.trim();
}

async function login(page, email, password) {
  await page.goto(`${baseUrl}/login-form`, { waitUntil: "networkidle" });
  await page.fill('input[name="email"]', email);
  await page.fill('input[name="password"]', password);
  await Promise.all([
    page.waitForURL(`${baseUrl}/`, { timeout: 10000 }),
    page.click('button[type="submit"]'),
  ]);
}

(async () => {
  const email = requireEnv(loginEmail, "CAPTURE_LOGIN_EMAIL");
  const password = requireEnv(loginPassword, "CAPTURE_LOGIN_PASSWORD");

  const outputDir = path.join(process.cwd(), "screenshots");
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }

  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({
    viewport: { width: 1440, height: 1024 },
  });

  try {
    await login(page, email, password);

    for (const item of pages) {
      const url = baseUrl + item.path;
      console.log("Capturing authenticated page:", url);

      await page.goto(url, { waitUntil: "networkidle" });
      await page.screenshot({
        path: path.join(outputDir, `${item.name}.png`),
        fullPage: true,
      });
    }
  } finally {
    await browser.close();
  }
})();
