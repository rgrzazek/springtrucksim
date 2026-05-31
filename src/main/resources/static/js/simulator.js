import { VEHICLE_TYPES } from "./truck_types.js";

const canvas = document.getElementById("simulator");
const ctx = canvas.getContext("2d");
const DEBUG = false;

canvas.width = 1000;
canvas.height = 1000;

const MAPS = {
  "emerald": {
    src: "/images/emerald-truck-stop.png",
    pixelsPerMetre: 4.5,
    spawnX: 563,
    spawnY: 876,
    spawnAngle: 5.32
  }
};

const MAP = MAPS["emerald"];
const bgImage = new Image();
bgImage.src = MAP.src;

const TRUCK_SPEED_MS = 10; // metres per second

const MAX_STEER_ANGLE = Math.PI / 5;
const LATERAL_RATIO = Math.tan(MAX_STEER_ANGLE);

// ── Configs ───────────────────────────────────────────────────────────────────
const CONFIGS = {
  "Semi": ["btrailer"],
  "B-Double": ["atrailer", "btrailer"],
  "AB Triple": ["btrailer", "dolly", "atrailer", "btrailer"],
  "A-Double": ["btrailer", "dolly", "btrailer"],
  "B-Triple": ["atrailer", "atrailer", "btrailer"],
  "A-Triple": ["btrailer", "dolly", "btrailer", "dolly", "btrailer"]
};

// ── Input ─────────────────────────────────────────────────────────────────────
canvas.tabIndex = 0;
canvas.focus();

const keys = { up: false, down: false, left: false, right: false };

canvas.addEventListener("keydown", (e) => {
  if (e.key === "ArrowUp") keys.up = true;
  if (e.key === "ArrowDown") keys.down = true;
  if (e.key === "ArrowLeft") keys.left = true;
  if (e.key === "ArrowRight") keys.right = true;
  e.preventDefault();
});
canvas.addEventListener("keyup", (e) => {
  if (e.key === "ArrowUp") keys.up = false;
  if (e.key === "ArrowDown") keys.down = false;
  if (e.key === "ArrowLeft") keys.left = false;
  if (e.key === "ArrowRight") keys.right = false;
});
const touch = { active: false, x: 0, y: 0 };

function canvasCoords(clientX, clientY) {
  const rect = canvas.getBoundingClientRect();
  return {
    x: (clientX - rect.left) * (canvas.width / rect.width),
    y: (clientY - rect.top) * (canvas.height / rect.height),
  };
}

canvas.addEventListener("touchstart", (e) => { e.preventDefault(); const p = canvasCoords(e.touches[0].clientX, e.touches[0].clientY); touch.active = true; touch.x = p.x; touch.y = p.y; }, { passive: false });
canvas.addEventListener("touchmove", (e) => { e.preventDefault(); const p = canvasCoords(e.touches[0].clientX, e.touches[0].clientY); touch.x = p.x; touch.y = p.y; }, { passive: false });
canvas.addEventListener("touchend", (e) => { e.preventDefault(); touch.active = false; }, { passive: false });
canvas.addEventListener("touchcancel", (e) => { e.preventDefault(); touch.active = false; }, { passive: false });

// ── Vehicle ───────────────────────────────────────────────────────────────────
class Vehicle {
  constructor(x, y, type, leader) {
    this.x = x;
    this.y = y;
    this.angle = 0;
    this.leader = leader;

    this.stats = { ...VEHICLE_TYPES[type] };
    Object.keys(this.stats).forEach(k => {
      if (typeof this.stats[k] === "number") this.stats[k] *= MAP.pixelsPerMetre;
    });
    if (leader == null) return;
    this.image = new Image();
    this.image.src = this.stats.image;
  }

  towpoint() {
    if (this.leader == null) return [this.x, this.y];
    return [
      this.x + Math.cos(this.angle) * this.stats.hitchOffset,
      this.y + Math.sin(this.angle) * this.stats.hitchOffset,
    ];
  }

  roll(deltaTime) {
    const speed = TRUCK_SPEED_MS * MAP.pixelsPerMetre * (deltaTime / 1000);

    if (this.leader == null) {
      this.angle = Math.atan2(this.follower.y - this.y, this.follower.x - this.x);
      // console.log(this.x, this.y, this.angle); // DEBUGGING - find out where truck is

      // Keyboard
      if (keys.up) {
        this.x -= Math.cos(this.angle) * speed;
        this.y -= Math.sin(this.angle) * speed;
        if (keys.left) { this.x += Math.cos(this.angle + Math.PI / 2) * speed * LATERAL_RATIO; this.y += Math.sin(this.angle + Math.PI / 2) * speed * LATERAL_RATIO; }
        if (keys.right) { this.x += Math.cos(this.angle - Math.PI / 2) * speed * LATERAL_RATIO; this.y += Math.sin(this.angle - Math.PI / 2) * speed * LATERAL_RATIO; }
      }
      if (keys.down) {
        this.x += Math.cos(this.angle) * speed;
        this.y += Math.sin(this.angle) * speed;
        if (keys.left) { this.x += Math.cos(this.angle - Math.PI / 2) * speed * LATERAL_RATIO; this.y += Math.sin(this.angle - Math.PI / 2) * speed * LATERAL_RATIO; }
        if (keys.right) { this.x += Math.cos(this.angle + Math.PI / 2) * speed * LATERAL_RATIO; this.y += Math.sin(this.angle + Math.PI / 2) * speed * LATERAL_RATIO; }
      }

      // Touch
      if (touch.active) {
        const drive = this.follower;
        const fwdX = this.x - drive.x;
        const fwdY = this.y - drive.y;
        const fwdLen = Math.sqrt(fwdX * fwdX + fwdY * fwdY);
        if (fwdLen === 0) return;
        const fwdAngle = Math.atan2(fwdY, fwdX);
        const tvX = touch.x - drive.x;
        const tvY = touch.y - drive.y;
        if (Math.sqrt(tvX * tvX + tvY * tvY) < 20) return;
        const touchAngle = Math.atan2(tvY, tvX);
        let angleToTouch = touchAngle - fwdAngle;
        angleToTouch = Math.atan2(Math.sin(angleToTouch), Math.cos(angleToTouch));

        if (Math.abs(angleToTouch) <= Math.PI / 2) {
          // Forward
          const clamped = Math.max(-MAX_STEER_ANGLE, Math.min(MAX_STEER_ANGLE, angleToTouch));
          this.x += Math.cos(fwdAngle + clamped) * speed;
          this.y += Math.sin(fwdAngle + clamped) * speed;
        } else {
          // Reverse — invert lateral offset
          const reverseAngle = fwdAngle + Math.PI;
          let reverseToTouch = touchAngle - reverseAngle;
          reverseToTouch = Math.atan2(Math.sin(reverseToTouch), Math.cos(reverseToTouch));
          const steerOffset = Math.max(-MAX_STEER_ANGLE, Math.min(MAX_STEER_ANGLE, -reverseToTouch));
          this.x += Math.cos(reverseAngle + steerOffset) * speed;
          this.y += Math.sin(reverseAngle + steerOffset) * speed;
        }
      }

      return;
    }

    const [x, y] = this.leader.towpoint();
    const currentDistance = Math.sqrt((x - this.x) ** 2 + (y - this.y) ** 2);
    this.angle = Math.atan2(y - this.y, x - this.x);
    const distance = currentDistance - this.stats.length;
    if (Math.abs(distance) < 1) return;
    this.x += Math.cos(this.angle) * distance;
    this.y += Math.sin(this.angle) * distance;
  }

  draw() {
    if (this.leader == null) return;
    const { width: w, length: ht, backOverhang: tail, frontOverhang: head } = this.stats;
    ctx.save();
    ctx.translate(this.x, this.y);
    ctx.rotate(this.angle + Math.PI / 2);
    ctx.drawImage(this.image, -w / 2, -(ht + head), w, ht + head + tail);
    ctx.restore();

    if (!DEBUG) return;
    const [x, y] = this.leader.towpoint();
    ctx.beginPath(); ctx.arc(Math.round(x), Math.round(y), 5, 0, Math.PI * 2); ctx.fillStyle = "red"; ctx.fill();
    ctx.beginPath(); ctx.arc(Math.round(this.x), Math.round(this.y), 5, 0, Math.PI * 2); ctx.fillStyle = "blue"; ctx.fill();
    ctx.beginPath(); ctx.moveTo(this.x, this.y); ctx.lineTo(x, y); ctx.strokeStyle = "black"; ctx.stroke();
  }
}

// ── Builder ───────────────────────────────────────────────────────────────────
function buildCombo(sx, sy, headingAngle, trailerTypes) {
  const bwd = headingAngle + Math.PI; // backward direction

  const steer = new Vehicle(sx, sy, null, null);

  const primeLength = VEHICLE_TYPES.prime.length * MAP.pixelsPerMetre;
  const drive = new Vehicle(
    sx + Math.cos(bwd) * primeLength,
    sy + Math.sin(bwd) * primeLength,
    "prime", steer
  );
  drive.angle = headingAngle;
  steer.follower = drive;

  const all = [steer, drive];

  // Walk backward from drive's tow point, placing each trailer
  let towX = drive.x; // prime hitchOffset = 0
  let towY = drive.y;
  let prev = drive;

  for (const type of trailerTypes) {
    const raw = VEHICLE_TYPES[type];
    const length = raw.length * MAP.pixelsPerMetre;
    const hitchOffset = raw.hitchOffset * MAP.pixelsPerMetre;

    const vx = towX + Math.cos(bwd) * length;
    const vy = towY + Math.sin(bwd) * length;

    const vehicle = new Vehicle(vx, vy, type, prev);
    vehicle.angle = headingAngle;
    all.push(vehicle);

    towX = vx + Math.cos(headingAngle) * hitchOffset;
    towY = vy + Math.sin(headingAngle) * hitchOffset;
    prev = vehicle;
  }

  return all;
}

// ── Init & combo switching ────────────────────────────────────────────────────
let vehicles = buildCombo(
  MAP.spawnX, MAP.spawnY,
  MAP.spawnAngle,
  CONFIGS["AB Triple"]
);

document.querySelectorAll("[data-combo]").forEach(btn => {
  btn.addEventListener("click", () => {
    const steer = vehicles[0];
    const heading = steer.angle + Math.PI; // steer.angle points backward
    vehicles = buildCombo(steer.x, steer.y, heading, CONFIGS[btn.dataset.combo]);
    document.querySelectorAll("[data-combo]").forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
    canvas.focus();
  });
});

// ── Loop ──────────────────────────────────────────────────────────────────────
function update(deltaTime) {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.drawImage(bgImage, 0, 0, canvas.width, canvas.height);
  vehicles.forEach(v => v.roll(deltaTime));
  vehicles.forEach(v => v.draw());
}

let lastTime = performance.now();
function loop(timestamp) {
  const deltaTime = timestamp - lastTime;
  lastTime = timestamp;
  update(deltaTime);
  requestAnimationFrame(loop);
}

loop(lastTime);

