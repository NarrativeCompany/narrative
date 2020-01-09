export function stripHtml (html: string) {
  const tempDivElement = document.createElement('div');
  // Set the HTML content with the provided string
  tempDivElement.innerHTML = html;
  // Retrieve the text property of the element (cross-browser support)
  return tempDivElement.textContent || tempDivElement.innerText || '';
}
