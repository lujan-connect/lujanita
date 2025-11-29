module.exports = {
  default: {
    requireModule: ["ts-node/register"],
    require: [
      "steps/support/env.ts",
      "steps/**/*.ts"
    ],
    paths: ["features/**/*.feature"],
    format: ["progress"]
  }
};
