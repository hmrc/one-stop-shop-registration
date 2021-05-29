#!/usr/bin/env bash
sbt -mem 2048 compile coverage test it:test coverageOff coverageReport
