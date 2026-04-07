const fs = require('fs');
const path = require('path');
const db = require('../db'); // Conexión MariaDB

const PUBLIC_DIR = path.join(__dirname, '../public');

// ========================
// Crear producto
// ========================
exports.crearProducto = async (req, res) => {
  try {
    const { nombre, variedad, precio, stock, categoriaId } = req.body;
    // Tomar el nombre que multer ya guardó
    const imagenNombre = req.file ? req.file.filename : null;

    const query = `
      INSERT INTO productos (nombre, variedad, categoria_id, precio, stock, imagen)
      VALUES (?, ?, ?, ?, ?, ?)
    `;
    const result = await db.query(query, [
      nombre,
      variedad,
      categoriaId,
      precio,
      stock,
      imagenNombre
    ]);

    // Retornar siempre un objeto producto para el frontend
    res.status(201).json({
      mensaje: "Producto creado",
      producto: {
        id: result.insertId,
        nombre,
        variedad,
        precio,
        stock,
        categoriaId,
        imagenProducto: imagenNombre || null
      }
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Error al crear producto" });
  }
};

// ========================
// Editar producto
// ========================
exports.editarProducto = async (req, res) => {
  try {
    const { id } = req.params;
    const { nombre, variedad, precio, stock, categoriaId } = req.body;
    const imagenNombre = req.file ? req.file.filename : null;

    const query = `
      UPDATE productos
      SET nombre=?, variedad=?, categoria_id=?, precio=?, stock=? ${imagenNombre ? ', imagen=?' : ''}
      WHERE id=?
    `;

    const params = imagenNombre
      ? [nombre, variedad, categoriaId, precio, stock, imagenNombre, id]
      : [nombre, variedad, categoriaId, precio, stock, id];

    await db.query(query, params);

    // Obtener el producto actualizado
    const rows = await db.query(
      'SELECT id, nombre, variedad, precio, stock, categoria_id AS categoriaId, imagen FROM productos WHERE id=?',
      [id]
    );

    if (!rows[0]) return res.status(404).json({ error: "Producto no encontrado" });

    const producto = rows[0];
    producto.imagenProducto = producto.imagen || null;
    delete producto.imagen;

    res.json({
      mensaje: "Producto actualizado",
      producto
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Error al actualizar producto" });
  }
};

// ========================
// Eliminar producto
// ========================
exports.eliminarProducto = async (req, res) => {
  try {
    const { id } = req.params;
    const producto = await db.query('SELECT imagen FROM productos WHERE id=?', [id]);

    // Borrar imagen si existe
    if (producto[0]?.imagen) {
      const filePath = path.join(PUBLIC_DIR, producto[0].imagen);
      if (fs.existsSync(filePath)) fs.unlinkSync(filePath);
    }

    await db.query('DELETE FROM productos WHERE id=?', [id]);

    res.json({ mensaje: "Producto eliminado", id });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Error al eliminar producto" });
  }
};