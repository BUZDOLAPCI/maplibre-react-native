import { promises as fs } from "node:fs";
import path from "node:path";

import { PACKAGE_PATH } from "../../utils/pathes";

async function getNativeVersion(pathSegments: string[], regex: RegExp) {
  const resolvedFilePath = path.join(PACKAGE_PATH, ...pathSegments);
  const lines = (await fs.readFile(resolvedFilePath, "utf8")).split("\n");
  const line = lines.filter((i) => regex.exec(i))[0];
  const version = line && regex.exec(line)?.[1];

  if (!version) {
    throw new Error("Could not find native version from " + resolvedFilePath);
  }

  return version;
}

let cachedAndroidVersion: string;
let cachedIosVersion: string;

export const getAndroidVersion = async () => {
  if (!cachedAndroidVersion) {
    cachedAndroidVersion = await getNativeVersion(
      ["android", "gradle.properties"],
      /^org\.maplibre\.reactnative\.nativeVersion=(\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?)$/,
    );
  }

  return cachedAndroidVersion;
};

export const getIosVersion = async () => {
  if (!cachedIosVersion) {
    cachedIosVersion = await getNativeVersion(
      ["MapLibreReactNative.podspec"],
      /^\$MLRN_NATIVE_VERSION \|\|= "(\d+\.\d+\.\d+)"$/,
    );
  }

  return cachedIosVersion;
};

export function isVersionGTE(
  version: string,
  otherVersion: string | undefined,
) {
  const numericVersion = version.match(/^(\d+\.\d+\.\d+)/)?.[1];

  return (
    !!numericVersion &&
    !!otherVersion?.match(/^(\d+\.\d+\.\d+)$/) &&
    numericVersion.localeCompare(otherVersion, undefined, {
      numeric: true,
      sensitivity: "base",
    }) >= 0
  );
}
