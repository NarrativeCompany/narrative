const path = require('path');
const fs = require('fs');
const mergeSchemas = require('merge-graphql-schemas');

const { fileLoader, mergeTypes } = mergeSchemas;
const { writeFileSync } = fs;

const mergeSchema = mergeTypes(fileLoader(path.join(__dirname, '../serverSchema')), { all: true });
writeFileSync('./schema/serverSchema.graphql', mergeSchema);
