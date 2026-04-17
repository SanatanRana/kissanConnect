import React, { useState, useEffect } from 'react';
import api from '../api';
import { useNavigate } from 'react-router-dom';

function Dashboard({ isAuthenticated }) {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const role = localStorage.getItem('role');
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) return;
    fetchProducts();
  }, [isAuthenticated]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const response = await api.get('/products');
      setProducts(response.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to fetch products');
    } finally {
      setLoading(false);
    }
  };

  const [orderQuantity, setOrderQuantity] = useState({});
  const [selectedCategory, setSelectedCategory] = useState('All');
  const categories = ['All', 'Vegetables', 'Fruits', 'Grains', 'Seeds'];

  const filteredProducts = selectedCategory === 'All' 
    ? products 
    : products.filter(p => p.category?.toLowerCase() === selectedCategory.toLowerCase());

  const handleQuantityChange = (id, val) => {
    setOrderQuantity(prev => ({...prev, [id]: val}));
  };

  const handleOrder = async (product) => {
    const qty = parseInt(orderQuantity[product.id] || 1, 10);
    if (qty > product.quantity) {
      alert(`Only ${product.quantity} available in stock.`);
      return;
    }
    try {
      await api.post('/orders', {
        customerId: localStorage.getItem('userId'),
        sellerId: product.sellerId,
        orderStatus: 'PLACED',
        items: [{
          productId: product.id,
          productName: product.name,
          quantity: qty,
          price: product.price,
          unit: product.unit
        }]
      });
      alert('Order placed successfully!');
      fetchProducts();
    } catch (err) {
      alert('Failed to place order: ' + (err.response?.data?.error || err.message));
    }
  };

  const [showAddForm, setShowAddForm] = useState(false);
  const [showUpdateForm, setShowUpdateForm] = useState(false);
  const [editingProductId, setEditingProductId] = useState(null);
  const [newProduct, setNewProduct] = useState({ name: '', price: '', description: '', category: 'vegetables', stockQuantity: '', unit: 'kg' });
  const [imageFile, setImageFile] = useState(null);

  const handleAddProduct = async (e) => {
    e.preventDefault();
    try {
      const formData = new FormData();
      formData.append('product', JSON.stringify({
        name: newProduct.name,
        price: parseFloat(newProduct.price),
        description: newProduct.description,
        category: newProduct.category,
        quantity: parseInt(newProduct.stockQuantity, 10),
        unit: newProduct.unit,
        sellerId: localStorage.getItem('userId')
      }));
      if (imageFile) {
        formData.append('image', imageFile);
      }
      
      if (editingProductId) {
        await api.put(`/products/${editingProductId}`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        alert('Product updated successfully!');
      } else {
        await api.post('/products', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        alert('Product created successfully!');
      }
      
      setShowAddForm(false);
      setEditingProductId(null);
      setImageFile(null);
      setNewProduct({ name: '', price: '', description: '', category: 'vegetables', stockQuantity: '', unit: 'kg' });
      fetchProducts();
    } catch (err) {
      alert(`Failed to ${editingProductId ? 'update' : 'list'} product: ` + (err.response?.data?.error || err.message));
    }
  };

  const handleEditClick = (product) => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    setEditingProductId(product.id);
    setNewProduct({
      name: product.name,
      price: product.price,
      description: product.description,
      category: product.category,
      stockQuantity: product.quantity,
      unit: product.unit
    });
    setImageFile(null);
    setShowAddForm(true);
  };

  const handleDeleteProduct = async (productId) => {
    if (!window.confirm('Are you sure you want to delete this product?')) {
      return;
    }
    try {
      const sellerId = localStorage.getItem('userId');
      await api.delete(`/products/${productId}?sellerId=${sellerId}`);
      alert('Product deleted successfully!');
      fetchProducts();
    } catch (err) {
      alert('Failed to delete product: ' + (err.response?.data?.error || err.message));
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="card fade-in" style={{ textAlign: 'center', marginTop: '2rem' }}>
        <h2>Welcome to Kissan Connect</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>
          Your ultimate marketplace for agricultural products. Please login or register to explore.
        </p>
        <button className="btn btn-primary" style={{ width: 'auto', marginRight: '1rem' }} onClick={() => navigate('/login')}>Login</button>
        <button className="btn" style={{ background: '#30363d', color: '#fff', width: 'auto' }} onClick={() => navigate('/register')}>Register</button>
      </div>
    );
  }

  return (
    <div className="fade-in">
      <div className="products-header">
        <h2>Marketplace</h2>
        {role === 'SELLER' ? (
          <button className="btn btn-primary" style={{ width: 'auto' }} onClick={() => {
            if (showAddForm) {
              setShowAddForm(false);
              setEditingProductId(null);
              setNewProduct({ name: '', price: '', description: '', category: 'vegetables', stockQuantity: '', unit: 'kg' });
            } else {
              setShowAddForm(true);
            }
          }}>
            {showAddForm ? 'Cancel' : '+ Add Product'}
          </button>
        ) : null}
      </div>

      {showAddForm && (
        <div className="card fade-in" style={{ marginBottom: '2rem' }}>
          <h3>{editingProductId ? 'Update Product' : 'Add New Product'}</h3>
          <form onSubmit={handleAddProduct} style={{ marginTop: '1rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="form-group">
                <label className="form-label">Name</label>
                <input type="text" className="form-control" value={newProduct.name} onChange={e => setNewProduct({...newProduct, name: e.target.value})} required />
              </div>
              <div className="form-group">
                <label className="form-label">Price</label>
                <input type="number" className="form-control" value={newProduct.price} onChange={e => setNewProduct({...newProduct, price: e.target.value})} required />
              </div>
              <div className="form-group">
                <label className="form-label">Category</label>
                <select className="form-control" value={newProduct.category} onChange={e => setNewProduct({...newProduct, category: e.target.value})}>
                  <option value="vegetables">Vegetables</option>
                  <option value="grains">Grains</option>
                  <option value="seeds">Seeds</option>
                  <option value="fruits">Fruits</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Unit</label>
                <input type="text" className="form-control" placeholder="kg, quintal, piece" value={newProduct.unit} onChange={e => setNewProduct({...newProduct, unit: e.target.value})} required />
              </div>
              <div className="form-group">
                <label className="form-label">Stock Quantity</label>
                <input type="number" className="form-control" value={newProduct.stockQuantity} onChange={e => setNewProduct({...newProduct, stockQuantity: e.target.value})} required />
              </div>
              <div className="form-group">
                <label className="form-label">Product Image</label>
                <input type="file" className="form-control" onChange={e => setImageFile(e.target.files[0])} />
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Description</label>
              <textarea className="form-control" rows="3" value={newProduct.description} onChange={e => setNewProduct({...newProduct, description: e.target.value})} required></textarea>
            </div>
            <button type="submit" className="btn btn-success">{editingProductId ? 'Save Changes' : 'Publish Product'}</button>
          </form>
        </div>
      )}
      
      {loading ? (
        <div className="loader"></div>
      ) : error ? (
        <div style={{ color: 'var(--danger)', textAlign: 'center' }}>{error}</div>
      ) : (
        <>
          {role === 'CUSTOMER' && (
            <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem', overflowX: 'auto', paddingBottom: '0.5rem' }}>
              {categories.map(cat => (
                <button
                  key={cat}
                  className="btn"
                  style={{
                    background: selectedCategory === cat ? 'var(--primary)' : '#30363d',
                    color: '#fff',
                    width: 'auto',
                    padding: '0.5rem 1rem',
                    borderRadius: '20px',
                    whiteSpace: 'nowrap',
                    border: 'none',
                    cursor: 'pointer'
                  }}
                  onClick={() => setSelectedCategory(cat)}
                >
                  {cat}
                </button>
              ))}
            </div>
          )}
          {filteredProducts.length === 0 ? (
            <div className="card" style={{ textAlign: 'center' }}>
              <p style={{ color: 'var(--text-muted)' }}>No products found.</p>
            </div>
          ) : (
            <div className="products-grid">
              {filteredProducts.map(product => (
            <div className="product-card" key={product.id}>
              <div className="product-image">
                {product.imageUrl ? (
                  <img src={`${import.meta.env.VITE_API_HOST}${product.imageUrl}`} alt={product.name} />
                ) : (
                  <span style={{ color: '#8b949e' }}>No Image</span>
                )}
              </div>
              <div className="product-info">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <h3 className="product-title">{product.name}</h3>
                  <span className="pill">{product.category}</span>
                </div>
                <div className="product-price">₹{product.price} <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>/ {product.unit}</span></div>
                <p className="product-desc">{product.description}</p>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto' }}>
                  <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Stock: {product.quantity}</span>
                  {role === 'CUSTOMER' && (
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                      <input 
                        type="number" 
                        min="1" 
                        max={product.quantity} 
                        value={orderQuantity[product.id] || 1} 
                        onChange={(e) => handleQuantityChange(product.id, e.target.value)} 
                        style={{ width: '60px', padding: '0.3rem', borderRadius: '4px', border: '1px solid var(--border)' }} 
                      />
                      <button className="btn btn-primary" style={{ padding: '0.5rem 1rem', width: 'auto' }} onClick={() => handleOrder(product)}>
                        Buy Now
                      </button>
                    </div>
                  )}
                  {role === 'SELLER' && product.sellerId === parseInt(localStorage.getItem('userId'), 10) && (
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button 
                        className="btn btn-primary" 
                        style={{ padding: '0.5rem 1rem', width: 'auto', border: 'none' }} 
                        onClick={() => handleEditClick(product)}
                      >
                        Update
                      </button>
                      <button 
                        className="btn" 
                        style={{ padding: '0.5rem 1rem', width: 'auto', background: 'var(--danger)', color: '#fff', border: 'none' }} 
                        onClick={() => handleDeleteProduct(product.id)}
                      >
                        Delete
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
          </div>
          )}
        </>
      )}
    </div>
  );
}

export default Dashboard;
