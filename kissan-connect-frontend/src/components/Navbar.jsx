import React from 'react';
import { Link } from 'react-router-dom';

function Navbar({ isAuthenticated, onLogout }) {
  return (
    <div className="navbar">
      <Link to="/" className="navbar-brand">Kissan Connect</Link>
      <div className="navbar-links">
        {isAuthenticated ? (
          <>
            <Link to="/">Dashboard</Link>
            <Link to="/products">Catalog</Link>
            <Link to="/cart">Cart</Link>
            <Link to="/orders">Orders</Link>
            <a href="#" onClick={(e) => { e.preventDefault(); onLogout(); }}>Logout</a>
          </>
        ) : (
          <>
            <Link to="/products">Catalog</Link>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </div>
  );
}

export default Navbar;
