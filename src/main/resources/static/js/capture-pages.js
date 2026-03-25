const { chromium } = require("playwright");
const fs = require("fs");
const path = require("path");

(async () => {
  const baseUrl = "http://localhost:8080";
  const pages = [
    { name: "home", path: "/" },
  { name: "login", path: "/login-form" },
  { name: "join", path: "/join-form" },
  { name: "boards", path: "/boards" },
  { name: "board-detail", path: "/boards/1" },
  { name: "calendar", path: "/calendar" },
  { name: "map-detail", path: "/bookings/map-detail" }
  ];

  const outputDir = path.join(process.cwd(), "screenshots");
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }

  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({
    viewport: { width: 1440, height: 1024 }
  });

  for (const item of pages) {
    const url = baseUrl + item.path;
    console.log("Capturing:", url);

    await page.goto(url, { waitUntil: "networkidle" });
    await page.screenshot({
      path: path.join(outputDir, `${item.name}.png`),
      fullPage: true
    });
  }

  await browser.close();
})();