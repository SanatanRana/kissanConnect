import React, { useState, useEffect } from 'react';
import api from '../api';

function Orders({ isAuthenticated }) {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    if (isAuthenticated) {
      fetchOrders();
    }
  }, [isAuthenticated]);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const role = localStorage.getItem('role');
      const userId = localStorage.getItem('userId');
      const response = await api.get('/orders');
      if (role === 'CUSTOMER') {
        setOrders(response.data.filter(o => o.customerId == userId));
      } else if (role === 'SELLER') {
        setOrders(response.data.filter(o => o.sellerId == userId));
      }
    } catch (err) {
      setError('Failed to fetch orders');
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated) return <div>Please login to view orders.</div>;

  return (
    <div className="fade-in">
      <h2>Order History</h2>
      {loading ? (
        <div className="loader"></div>
      ) : error ? (
        <div style={{ color: 'var(--danger)' }}>{error}</div>
      ) : orders.length === 0 ? (
        <div className="card">No orders found.</div>
      ) : (
        <div className="card">
          <table style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border)' }}>
                <th style={{ padding: '0.5rem' }}>Order ID</th>
                <th style={{ padding: '0.5rem' }}>Date</th>
                <th style={{ padding: '0.5rem' }}>Items</th>
                <th style={{ padding: '0.5rem' }}>Total Amount</th>
                <th style={{ padding: '0.5rem' }}>Status</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => (
                <tr key={order.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ padding: '0.5rem' }}>#{order.id}</td>
                  <td style={{ padding: '0.5rem' }}>{new Date(order.orderDate).toLocaleString()}</td>
                  <td style={{ padding: '0.5rem' }}>
                    {order.items?.map(item => (
                      <div key={item.id}>{item.productName} ({item.quantity} {item.unit})</div>
                    ))}
                  </td>
                  <td style={{ padding: '0.5rem' }}>?{order.totalAmount}</td>
                  <td style={{ padding: '0.5rem' }}>
                    <span className="pill">{order.orderStatus}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default Orders;
