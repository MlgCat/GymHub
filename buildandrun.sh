#!/bin/sh
npm run build -C functions/;
firebase emulators:start --only functions;
