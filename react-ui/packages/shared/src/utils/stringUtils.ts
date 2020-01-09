export function truncateStringInMiddle(str: string, leftLength: number, rightLength: number) {
  const separator = '...';
  const sepLen = separator.length;
  const totalLen = leftLength + sepLen + rightLength;
  const strLen = str.length;

  if (strLen <= totalLen) {
    return str;
  }

  return str.substr(0, leftLength) +
    separator +
    str.substr(strLen - rightLength);
}
