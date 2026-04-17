import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api';

function ProductDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    fetchProductDetails();
  }, [id]);

  const fetchProductDetails = async () => {
    try {
      const response = await api.get(`/products/${id}`);
      setProduct(response.data);
      setLoading(false);
    } catch (err) {
      setError('Failed to load product details');
      setLoading(false);
    }
  };

  const handleAddToCart = async () => {
    const userId = localStorage.getItem('userId');
    if (!userId) {
      alert("Please login to add items to your cart.");
      navigate('/login');
      return;
    }

    try {
      await api.post(`/orders/cart/${userId}/items`, {
        productId: product.id,
        quantity: parseInt(quantity, 10),
        price: product.price
      });
      alert('Product added to cart!');
      navigate('/cart');
    } catch (err) {
      alert('Failed to add to cart: ' + (err.response?.data?.message || err.message));
    }
  };

  if (loading) return <div>Loading product details...</div>;
  if (error) return <div style={{ color: 'red' }}>{error}</div>;
  if (!product) return <div>Product not found</div>;

  return (
    <div className="product-details" style={{ maxWidth: '600px', margin: '0 auto', padding: '20px' }}>
      <h2>{product.name}</h2>
      {product.imageUrl && (
        <img 
          src={product.imageUrl.startsWith("http") ? product.imageUrl : `http://localhost:8080${product.imageUrl}`} 
          alt={product.name} 
          style={{ width: '100%', maxHeight: '400px', objectFit: 'contain', marginBottom: '20px' }} 
        />
      )}
      <p style={{ fontSize: '1.2em' }}>{product.description}</p>
      <p><strong>Stock Available:</strong> {product.stockQuantity}</p>
      <p style={{ fontSize: '1.5em', color: 'green' }}><strong>Price:</strong> ${product.price?.toFixed(2)}</p>
      
      <div style={{ marginTop: '20px', display: 'flex', alignItems: 'center', gap: '10px' }}>
        <label>Quantity: </label>
        <input 
          type="number" 
          min="1" 
          max={product.stockQuantity || 100}
          value={quantity} 
          onChange={(e) => setQuantity(e.target.value)}
          style={{ width: '60px', padding: '5px' }}
        />
        <button 
          onClick={handleAddToCart}
          style={{ padding: '10px 20px', backgroundColor: '#e67e22', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Add to Cart
        </button>
      </div>
    </div>
  );
}

export default ProductDetails;
