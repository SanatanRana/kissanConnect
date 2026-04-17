import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

function Cart() {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const userId = localStorage.getItem('userId');

  useEffect(() => {
    if (!userId) {
      navigate('/login');
    } else {
      fetchCart();
    }
  }, [userId, navigate]);

  const fetchCart = async () => {
    try {
      const response = await api.get(`/orders/cart/${userId}`);
      setCart(response.data);
      setLoading(false);
    } catch (err) {
      setError('Failed to load cart');
      setLoading(false);
    }
  };

  const handleUpdateQuantity = async (productId, newQuantity) => {
    if (newQuantity < 1) return handleDeleteItem(productId);
    try {
      await api.put(`/orders/cart/${userId}/items/${productId}`, {
        quantity: newQuantity
      });
      fetchCart();
    } catch (err) {
      alert('Failed to update quantity');
    }
  };

  const handleDeleteItem = async (productId) => {
    try {
      await api.delete(`/orders/cart/${userId}/items/${productId}`);
      fetchCart();
    } catch (err) {
      alert('Failed to delete item');
    }
  };

  const handleCheckout = async () => {
    try {
      const response = await api.post(`/orders/checkout/${userId}`);
      alert(`Order placed successfully! Order ID: ${response.data.id || 'Confirmed'}`);
      navigate('/orders');
    } catch (err) {
      alert('Checkout failed: ' + (err.response?.data?.message || err.message));
    }
  };

  if (loading) return <div>Loading cart...</div>;
  if (error) return <div style={{ color: 'red' }}>{error}</div>;

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h2>Your Cart</h2>
      {!cart || !cart.items || cart.items.length === 0 ? (
        <p>Your cart is empty.</p>
      ) : (
        <div>
          <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '20px' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #ccc', textAlign: 'left' }}>
                <th style={{ padding: '10px' }}>Product ID</th>
                <th style={{ padding: '10px' }}>Price</th>
                <th style={{ padding: '10px' }}>Quantity</th>
                <th style={{ padding: '10px' }}>Total</th>
                <th style={{ padding: '10px' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {cart.items.map((item) => (
                <tr key={item.productId} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: '10px' }}>{item.productId}</td>
                  <td style={{ padding: '10px' }}>${item.price?.toFixed(2)}</td>
                  <td style={{ padding: '10px' }}>
                    <button onClick={() => handleUpdateQuantity(item.productId, item.quantity - 1)}>-</button>
                    <span style={{ margin: '0 10px' }}>{item.quantity}</span>
                    <button onClick={() => handleUpdateQuantity(item.productId, item.quantity + 1)}>+</button>
                  </td>
                  <td style={{ padding: '10px' }}>${(item.price * item.quantity).toFixed(2)}</td>
                  <td style={{ padding: '10px' }}>
                    <button onClick={() => handleDeleteItem(item.productId)} style={{ color: 'red', cursor: 'pointer' }}>Remove</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: '20px' }}>
            <h3>Total: ${cart.totalAmount?.toFixed(2)}</h3>
            <button 
              onClick={handleCheckout}
              style={{ padding: '10px 20px', backgroundColor: '#27ae60', color: 'white', border: 'none', borderRadius: '4px', fontSize: '1.1em', cursor: 'pointer' }}
            >
              Checkout
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Cart;
