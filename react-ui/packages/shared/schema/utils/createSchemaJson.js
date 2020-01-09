const path = require('path');
const fs = require('fs');
const { buildSchema, graphqlSync, introspectionQuery } = require('graphql');
// const { makeExecutableSchema } = require('graphql-tools');

const { readFileSync, writeFileSync } = fs;

const schemaText = readFileSync(path.join(__dirname, '../serverSchema.graphql'), 'utf8');
// bl: buildSchema does essentially the same thing as makeExecutableSchema (in a simplified fashion)
const jsSchema = buildSchema(schemaText);
// const jsSchema = makeExecutableSchema({
//   typeDefs: schemaText
// });

const introspectionResult = graphqlSync(jsSchema, introspectionQuery).data;
const schemaJson = JSON.stringify(introspectionResult, null, 2);

writeFileSync('./schema.json', schemaJson);

