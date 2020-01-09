export function stripUndefinedProperties<T>(object: T) {
  if (!object) {
    return object;
  }

  const result = {};
  Object.keys(object).forEach((key) => {
    const value = object[key];

    if (value !== null && value !== undefined) {
      result[key] = value;
    }
  });

  return result;
}

/**
 * jw: The intent of this function is to parallel `Omit`, except with an object instance of an interface so that we can
 *     take an object and omit some of its properties from the resulting object. This is useful for converting apollo
 *     types into mirror input objects (where we want to omit the __type property) or similar purposes.
 * @param object The object we want to omit some properties from.
 * @param omitKeys The keys from the object that we want to omit from the result.
 */
export function omitProperties<T, K extends keyof T>(object: T, omitKeys: K[]) {
  // jw: if we were not given an object then just return whatever we were given (undefined or null)
  if (!object) {
    return object;
  }

  // jw: if we were not given any keys then we have nothing to strip off
  if (!omitKeys || !omitKeys.length) {
    return object;
  }

  // jw: for performance, let's create a lookup of keys that should be removed.
  const omitLookup = omitKeys.reduce((acc, omitKey) => {
    acc[omitKey as string] = true;

    return acc;
  }, {});

  const result = {};
  Object.keys(object).forEach((key) => {
    // jw: if the key is in the ignored set then just short out.
    if (omitLookup[key]) {
      return;
    }

    result[key] = object[key];
  });

  return result;
}

export function chunkArray<T>(array: T[], chunkSize: number): T[][] {
    const chunked: T[][] = [];

    for (let index = 0; index < array.length; index += chunkSize) {
        const chunk: T[] = array.slice(index, index + chunkSize);
        chunked.push(chunk);
    }

    return chunked;
}

export function isPrerenderUserAgent() {
  return /prerender/i.test(navigator.userAgent);
}

export function getPropertyByPath(obj: {}, path: string) {
    const arr = path.split('.');
    while (arr.length) {
      obj = obj[arr.shift() as string];

      if (!obj) {
        return undefined;
      }
    }
    return obj;
}

// jw: while simple, this function is extremely useful for creating a lookup object from a collection of enum values
//     where the keys of the objects are enums from the values, and the values are all true. Allowing you to do tests
//     like `if (lookup[enum]) {` for presence of the enum instead of a indexOf() check which is less than performent.
export function getEnumLookupObject<T>(values: T[]) {
  if (!values.length) {
    return {};
  }

  return values.reduce((acc, role) => {
    acc[role.toString()] = true;

    return acc;
  }, {});
}
