import { List, Skeleton } from 'antd';
import { Card } from '../components/Card';
import * as React from 'react';

export function renderLoadingCard(cardNumber: number): JSX.Element {
  return (
    <List.Item key={cardNumber}>
      <Card loading={true}/>
    </List.Item>
  );
}

export function renderSkeleton(cardNumber: number): JSX.Element {
  return (
    <List.Item key={cardNumber}>
      <Skeleton title={true} active={true}/>
    </List.Item>
  );
}

export function generateSkeletonListProps(count: number, renderItem: (index: number) => JSX.Element) {
  if (count <= 0) {
    return { dataSource: [], renderItem: () => null};
  }

  return { dataSource: generateDummyData(count), renderItem };
}

export function generateDummyData(count: number): number[] {
  if (count <= 0) {
    return [];
  }

  const data: number[] = [];

  for (let i = 0; i < count; i++) {
    data.push(i);
  }

  return data;
}
