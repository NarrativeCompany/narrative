// tslint:disable no-any variable-name
export function getIntrospectionQueryResultData () {
  const schema = require('../../schema.json');
  const filteredSchema = schema.__schema.types.filter((type: any) => type.possibleTypes !== null);
  const __schema = {} as any;
  __schema.types = filteredSchema;

  return { __schema };
}
