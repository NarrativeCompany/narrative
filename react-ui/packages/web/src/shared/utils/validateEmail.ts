const expression = /\S+@\S+/;

export function validateEmail (emailAddress?: string) {
  if (!emailAddress) {
    return;
  }

  return expression.test(emailAddress.toLowerCase()) ? emailAddress : undefined;
}
