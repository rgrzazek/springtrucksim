const canvas = document.getElementById("simulator");
const ctx = canvas.getContext("2d");

canvas.width = 1000;
canvas.height = 1000;

class Vehicle {
  constructor(x, y, length, leader) {
    this.x = x;
    this.y = y;
    this.length = length;
    this.leader = leader;
  }

  roll(deltaTime) {
    let x, y, distance; // Where to aim for, what distance to aim for
    if (this.leader == null) {
      x = mouseX, y = mouseY, distance = 0
    } else {
      x = this.leader.x, y = this.leader.y, distance = this.length
    }

    const currentDistance = Math.sqrt((x - this.x) ** 2 + (y - this.y) ** 2);
    if (Math.abs(currentDistance - distance) < 2) return;

    const angle = Math.atan2((y - this.y), (x - this.x));
    const speed = 0.1 * deltaTime;

    if (currentDistance < distance) {
      this.x -= Math.cos(angle) * Math.min(currentDistance, speed);
      this.y -= Math.sin(angle) * Math.min(currentDistance, speed);
    } else {
      this.x += Math.cos(angle) * Math.min(currentDistance, speed);
      this.y += Math.sin(angle) * Math.min(currentDistance, speed);
    }
  }

  draw() {
    ctx.beginPath();
    ctx.arc(Math.round(this.x), Math.round(this.y), 10, 0, Math.PI * 2);
    ctx.fillStyle = "blue";
    ctx.fill();

    if (this.leader == null) return;
    ctx.beginPath();
    ctx.moveTo(this.x, this.y);
    ctx.lineTo(this.leader.x, this.leader.y);
    ctx.strokeStyle = "black";
    ctx.stroke();

  }
}

// Vehicles MUST be listed from the front of the truck to model correctly
const steer = new Vehicle(canvas.width / 2, canvas.height / 2, 0, null);
const drive = new Vehicle(canvas.width / 2, canvas.height / 2, 120, steer);
const trailer = new Vehicle(canvas.width / 2, canvas.height / 2, 240, drive);
const vehicles = [steer, drive, trailer]

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
