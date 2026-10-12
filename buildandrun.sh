#!/bin/sh
PORT = 8080
npm run build -C functions/;
firebase emulators:start --only functions;
