import { VEHICLE_TYPES } from "./truck_types.js";
const canvas = document.getElementById("simulator");
const ctx = canvas.getContext("2d");
const DEBUG = false

canvas.width = 1000;
canvas.height = 1000;
const PIXELS_PER_METRE = 10


class Vehicle {
  constructor(x, y, type, leader) {
    this.x = x;
    this.y = y;
    this.leader = leader;

    this.stats = { ...VEHICLE_TYPES[type] };
    Object.keys(this.stats).forEach(key => {
      if (typeof this.stats[key] === "number") {
        this.stats[key] *= PIXELS_PER_METRE;
      } // All numbers are metres, convert to pixels
    });
    if (leader == null) return;
    this.image = new Image();
    this.image.src = this.stats.image;
  }
  towpoint() {
    // Special case for steer axle
    if (this.leader == null) return [this.x, this.y]
    // Is the towpoint lined up with the trailing point of the vehicle?
    const offset = (this.stats.hitchOffset)

    const x = this.x + Math.cos(this.angle) * offset
    const y = this.y + Math.sin(this.angle) * offset
    return [x, y]
  }

  roll(deltaTime) {
    let x, y, distance; // Where to aim for, what distance to aim for
    if (this.leader == null) { // The front of the combo is user-controlled
      x = mouseX, y = mouseY, distance = 0
    } else { // everything except the front is determined by its leader
      [x, y] = this.leader.towpoint()
      distance = this.stats.length
    }

    const currentDistance = Math.sqrt((x - this.x) ** 2 + (y - this.y) ** 2);
    // positive x-axis = 0 radians
    this.angle = Math.atan2((y - this.y), (x - this.x));

    // Don't jitter
    if (Math.abs(currentDistance - distance) < 1) return;

    const speed = 0.1 * deltaTime;

    // Although it's physically impossible for a trailer (which shortcuts) to
    // go faster than its lead, speed limit everything for simplicity.
    if (currentDistance < distance) {
      this.x -= Math.cos(this.angle) * Math.min(currentDistance, speed);
      this.y -= Math.sin(this.angle) * Math.min(currentDistance, speed);
    } else {
      this.x += Math.cos(this.angle) * Math.min(currentDistance, speed);
      this.y += Math.sin(this.angle) * Math.min(currentDistance, speed);
    }
  }

  draw() {
    // The very front axle is a "dummy" Vehicle
    if (this.leader == null) return;

    const w = this.stats.width;
    const ht = this.stats.length;
    const tail = this.stats.backOverhang;
    const head = this.stats.frontOverhang;

    ctx.save(); // Save the current state
    ctx.translate(this.x, this.y); // Move to vehicle position
    ctx.rotate(this.angle + Math.PI / 2); // Rotate to the correct direction
    ctx.drawImage(this.image, -w / 2, -(ht + head), w, (ht + head + tail));
    ctx.restore();

    if (!DEBUG) return

    /* The following dots should be close except for on dollies */
    /* Red indicator dot: leading pivot point (front hitch) */
    ctx.beginPath();
    const [x, y] = this.leader.towpoint()
    ctx.arc(Math.round(x), Math.round(y), 5, 0, Math.PI * 2);
    ctx.fillStyle = "red";
    ctx.fill();
    /* Blue indicator dot: rear pivot point (back wheels) */
    ctx.beginPath();
    ctx.arc(Math.round(this.x), Math.round(this.y), 5, 0, Math.PI * 2);
    ctx.fillStyle = "blue";
    ctx.fill();

    if (this.leader == null) return;
    ctx.beginPath();
    ctx.moveTo(this.x, this.y);
    ctx.lineTo(x, y);
    ctx.strokeStyle = "black";
    ctx.stroke();


  }
}



// Vehicles MUST be listed from the front of the truck to model correctly
const steer = new Vehicle(canvas.width / 2, canvas.height / 2, null, null);
const drive = new Vehicle(canvas.width / 2, canvas.height / 2, "prime", steer);
const lead = new Vehicle(canvas.width / 2, canvas.height / 2, "btrailer", drive);
const dolly = new Vehicle(canvas.width / 2, canvas.height / 2, "dolly", lead);
const atrailer = new Vehicle(canvas.width / 2, canvas.height / 2, "atrailer", dolly);
const btrailer = new Vehicle(canvas.width / 2, canvas.height / 2, "btrailer", atrailer);
const vehicles = [steer, drive, lead, dolly, atrailer, btrailer]


let mouseX = canvas.width / 2, mouseY = canvas.height / 2;
canvas.addEventListener("mousemove", (event) => {
  const rect = canvas.getBoundingClientRect();
  mouseX = event.clientX - rect.left;
  mouseY = event.clientY - rect.top;
});

function update(deltaTime) {
  ctx.clearRect(0, 0, canvas.width, canvas.height); // Clear background
  vehicles.forEach(v => v.roll(deltaTime));
  vehicles.forEach(v => v.draw());
}


let lastTime = performance.now();
function loop(timestamp) {
  let deltaTime = timestamp - lastTime;
  lastTime = timestamp;
  update(deltaTime);
  requestAnimationFrame(loop);
}

loop(lastTime);
