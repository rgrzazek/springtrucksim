export const VEHICLE_TYPES = {
  prime: {
    image: "../images/prime.png",
    length: 4, // inner length (front pivot to rear wheels)
    width: 2.5,
    frontOverhang: 1.5, // Distance forward from front pivot point
    backOverhang: 1, // Distance back from centre of (back) wheels
    hitchOffset: 0 // distance from (back) wheels to hitch

  },
  atrailer: {
    image: "../images/atrailer.png",
    length: 6,
    width: 2.5,
    frontOverhang: 2,
    backOverhang: 1.5,
    hitchOffset: -0.5
  },
  btrailer: {
    image: "../images/btrailer.png",
    length: 7.5,
    width: 2.5,
    frontOverhang: 1.25,
    backOverhang: 2,
    hitchOffset: -2
  },
  dolly: {
    image: "../images/dolly.png",
    length: 3.5,
    width: 2.5,
    frontOverhang: 0,
    backOverhang: 1.5,
    hitchOffset: 0
  }
};