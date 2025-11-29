#!/usr/bin/env node
'use strict';

const { spawn, spawnSync } = require('node:child_process');
const { existsSync } = require('node:fs');
const path = require('node:path');

const rawArgs = process.argv.slice(2);
let requestedPython = process.env.UVICORN_PYTHON || process.env.PYTHON_INTERPRETER || null;
let requestedVenv = process.env.UVICORN_VENV_PATH || null;
let pythonVersionSpec =
  process.env.UVICORN_PYTHON_VERSION || process.env.PYTHON_VERSION_SPEC || process.env.PYTHON_VERSION || null;

const uvicornArgs = [];

for (let index = 0; index < rawArgs.length; index += 1) {
  const arg = rawArgs[index];

  if (arg === '--python' && index + 1 < rawArgs.length) {
    requestedPython = rawArgs[index + 1];
    index += 1;
    continue;
  }

  if (arg === '--venv' && index + 1 < rawArgs.length) {
    requestedVenv = rawArgs[index + 1];
    index += 1;
    continue;
  }

  if (arg === '--python-version' && index + 1 < rawArgs.length) {
    pythonVersionSpec = rawArgs[index + 1];
    index += 1;
    continue;
  }

  uvicornArgs.push(arg);
}

if (uvicornArgs.length === 0) {
  console.error(
    'Usage: node run-uvicorn.cjs [--python <path>] [--venv <path>] [--python-version <x.y>] <app> [uvicorn options...]'
  );
  process.exit(1);
}

const cwd = process.cwd();
const isWindows = process.platform === 'win32';
const venvPath = requestedVenv
  ? path.resolve(cwd, requestedVenv)
  : path.join(cwd, '.venv');

function commandExists(command) {
  const checkCommand = isWindows ? 'where' : 'which';
  const result = spawnSync(checkCommand, [command], { stdio: 'ignore' });
  return result.status === 0;
}

function resolveInterpreterCandidate(candidate) {
  if (!candidate) {
    return null;
  }

  if (path.isAbsolute(candidate)) {
    return existsSync(candidate) ? candidate : null;
  }

  const hasPathSeparator = candidate.includes(path.sep) || candidate.includes('/');
  if (hasPathSeparator) {
    const resolved = path.resolve(cwd, candidate);
    return existsSync(resolved) ? resolved : null;
  }

  return commandExists(candidate) ? candidate : null;
}

const venvCandidates = isWindows
  ? [path.join(venvPath, 'Scripts', 'python.exe'), path.join(venvPath, 'Scripts', 'python')]
  : [path.join(venvPath, 'bin', 'python3'), path.join(venvPath, 'bin', 'python')];

let pythonExecutable = null;
let interpreterSource = null;

const resolvedManual = resolveInterpreterCandidate(requestedPython);
if (resolvedManual) {
  pythonExecutable = resolvedManual;
  interpreterSource = 'manual';
} else if (requestedPython) {
  console.warn(
    'Requested Python interpreter "%s" could not be found. Falling back to automatic detection.',
    requestedPython
  );
}

if (!pythonExecutable) {
  for (const candidate of venvCandidates) {
    if (existsSync(candidate)) {
      pythonExecutable = candidate;
      interpreterSource = 'virtualenv';
      break;
    }
  }
}

let usingFallbackInterpreter = false;

if (!pythonExecutable) {
  const fallbackCandidates = isWindows
    ? ['py', 'python3.11', 'python3', 'python']
    : ['python3.11', 'python3', 'python'];

  for (const candidate of fallbackCandidates) {
    const resolvedCandidate = resolveInterpreterCandidate(candidate);
    if (resolvedCandidate) {
      pythonExecutable = resolvedCandidate;
      interpreterSource = 'system';
      usingFallbackInterpreter = true;
      break;
    }
  }
}

if (!pythonExecutable) {
  console.error(
    'Unable to locate a Python interpreter. Make sure Python is installed or create a virtual environment at %s.',
    venvPath
  );

  if (isWindows) {
    console.error(
      'Windows example: py -3.11 -m venv .venv && .\\.venv\\Scripts\\python.exe -m pip install -r requirements.txt'
    );
  } else {
    console.error(
      'Unix example: python3.11 -m venv .venv && ./.venv/bin/python -m pip install -r requirements.txt'
    );
  }

  console.error(
    'You can also specify an interpreter manually with "--python <path>" or the UVICORN_PYTHON environment variable.'
  );
  process.exit(1);
}

if (interpreterSource === 'manual') {
  console.info('Using Python interpreter "%s".', pythonExecutable);
} else if (usingFallbackInterpreter) {
  console.warn(
    'Using system Python interpreter "%s" because no virtual environment was found at %s.',
    pythonExecutable,
    venvPath
  );
}

const pythonArgs = [];

if (isWindows) {
  const executableName = path.basename(pythonExecutable).toLowerCase();
  const isPyLauncher = executableName === 'py.exe' || executableName === 'py';

  if (isPyLauncher && pythonVersionSpec) {
    const sanitized = pythonVersionSpec.startsWith('-') ? pythonVersionSpec : `-${pythonVersionSpec}`;
    pythonArgs.push(sanitized);
  }
}

const child = spawn(pythonExecutable, [...pythonArgs, '-m', 'uvicorn', ...uvicornArgs], {
  cwd,
  stdio: 'inherit',
  env: (() => {
    // Preserve existing env and add PYTHONPATH pointing to repo root (two levels above app cwd)
    const childEnv = Object.assign({}, process.env);
    try {
      const repoRoot = path.resolve(cwd, '..', '..');
      const existing = childEnv.PYTHONPATH || childEnv.PATH || '';
      // Use platform delimiter to join paths if necessary
      const delim = path.delimiter || (process.platform === 'win32' ? ';' : ':');
      // Prepend repoRoot so imports like `apps.common` resolve
      childEnv.PYTHONPATH = [repoRoot, childEnv.PYTHONPATH].filter(Boolean).join(delim);
    } catch (e) {
      // if anything goes wrong, fallback to existing env
    }
    return childEnv;
  })(),
});

child.on('exit', (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
  } else {
    process.exit(code ?? 0);
  }
});

child.on('error', (error) => {
  console.error('Failed to start uvicorn:', error);
  process.exit(1);
});
