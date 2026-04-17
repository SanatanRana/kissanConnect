import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api';

function ProductCatalog() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await api.get('/products');
      setProducts(response.data || []);
      setLoading(false);
    } catch (err) {
      setError('Failed to load products');
      setLoading(false);
    }
  };

  if (loading) return <div>Loading products...</div>;
  if (error) return <div style={{ color: 'red' }}>{error}</div>;

  return (
    <div>
      <h2>Product Catalog</h2>
      <div className="product-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px' }}>
        {products.map(product => (
          <div key={product.id} className="product-card" style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '5px' }}>
            <h3>{product.name}</h3>
            {product.imageUrl && (
              <img 
                src={product.imageUrl.startsWith("http") ? product.imageUrl : `http://localhost:8080${product.imageUrl}`} 
                alt={product.name} 
                style={{ width: '100%', height: '150px', objectFit: 'cover' }} 
              />
            )}
            <p>{product.description}</p>
            <p><strong>Price:</strong> ${product.price?.toFixed(2)}</p>
            <Link to={`/products/${product.id}`}>
              <button>View Details</button>
            </Link>
          </div>
        ))}
        {products.length === 0 && <p>No products available.</p>}
      </div>
    </div>
  );
}

export default ProductCatalog;
