"""
ONNX to TFLite Converter Script
===============================
Converts ONNX models to TFLite format using TensorFlow 2.14.0
This ensures compatibility with the Android TFLite runtime.

Requirements:
    pip install tensorflow==2.14.0 onnx onnx-tf tf2onnx

Usage:
    python convert_onnx_to_tflite.py

Input: ONNX models from models_backup folder
Output: TFLite models compatible with TFLite 2.14.0
"""

import os
import sys
import shutil

def check_dependencies():
    """Check if all required packages are installed"""
    missing = []

    try:
        import tensorflow as tf
        print(f"✅ TensorFlow: {tf.__version__}")
        if not tf.__version__.startswith("2.14"):
            print(f"   ⚠️  Recommended: tensorflow==2.14.0")
    except ImportError:
        missing.append("tensorflow==2.14.0")

    try:
        import onnx
        print(f"✅ ONNX: {onnx.__version__}")
    except ImportError:
        missing.append("onnx")

    try:
        import onnx_tf
        print(f"✅ ONNX-TF: {onnx_tf.__version__}")
    except ImportError:
        missing.append("onnx-tf")

    if missing:
        print(f"\n❌ Missing packages: {', '.join(missing)}")
        print("Install with:")
        print(f"    pip install {' '.join(missing)}")
        return False

    return True

def convert_onnx_to_tflite(onnx_path, tflite_path, temp_dir):
    """Convert ONNX model to TFLite format"""
    import tensorflow as tf
    import onnx
    from onnx_tf.backend import prepare

    model_name = os.path.basename(onnx_path)
    print(f"\n{'='*60}")
    print(f"Converting: {model_name}")
    print(f"{'='*60}")

    try:
        # Step 1: Load ONNX model
        print("Step 1: Loading ONNX model...")
        onnx_model = onnx.load(onnx_path)
        onnx.checker.check_model(onnx_model)
        print(f"  ✅ ONNX model loaded")

        # Get model info
        graph = onnx_model.graph
        inputs = [i.name for i in graph.input]
        outputs = [o.name for o in graph.output]
        print(f"  Inputs: {inputs}")
        print(f"  Outputs: {outputs}")

        # Step 2: Convert ONNX to TensorFlow
        print("Step 2: Converting to TensorFlow...")
        tf_rep = prepare(onnx_model)

        # Save as TF SavedModel
        saved_model_path = os.path.join(temp_dir, model_name.replace('.onnx', '_saved_model'))
        tf_rep.export_graph(saved_model_path)
        print(f"  ✅ TensorFlow SavedModel created")

        # Step 3: Convert TensorFlow to TFLite
        print("Step 3: Converting to TFLite...")
        converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_path)

        # Optimization settings
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_types = [tf.float32]

        # Allow custom ops for better compatibility
        converter.allow_custom_ops = False
        converter.experimental_new_converter = True

        tflite_model = converter.convert()

        # Save TFLite model
        with open(tflite_path, 'wb') as f:
            f.write(tflite_model)

        file_size_mb = os.path.getsize(tflite_path) / (1024 * 1024)
        print(f"  ✅ TFLite model saved: {tflite_path}")
        print(f"  Size: {file_size_mb:.2f} MB")

        # Verify the model works
        print("Step 4: Verifying TFLite model...")
        interpreter = tf.lite.Interpreter(model_path=tflite_path)
        interpreter.allocate_tensors()

        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()

        print(f"  ✅ Model verified!")
        print(f"  Input: {input_details[0]['shape']} ({input_details[0]['dtype']})")
        print(f"  Output: {output_details[0]['shape']} ({output_details[0]['dtype']})")

        # Cleanup temp SavedModel
        shutil.rmtree(saved_model_path, ignore_errors=True)

        return True

    except Exception as e:
        print(f"  ❌ Conversion failed: {e}")
        import traceback
        traceback.print_exc()
        return False

def main():
    print("=" * 60)
    print("ONNX to TFLite Converter")
    print("For Android TFLite Runtime Compatibility")
    print("=" * 60)

    if not check_dependencies():
        sys.exit(1)

    # Paths
    onnx_dir = r"D:\App\Intelli_PEST\models_backup"
    output_dir = r"D:\App\Intelli_PEST\app\src\main\assets\models"
    temp_dir = r"D:\App\Intelli_PEST\temp_conversion"

    # Create directories
    os.makedirs(output_dir, exist_ok=True)
    os.makedirs(temp_dir, exist_ok=True)

    # Find ONNX models
    onnx_files = [f for f in os.listdir(onnx_dir) if f.endswith('.onnx')]

    if not onnx_files:
        print(f"\n❌ No ONNX files found in {onnx_dir}")
        sys.exit(1)

    print(f"\nFound {len(onnx_files)} ONNX models:")
    for f in onnx_files:
        size_mb = os.path.getsize(os.path.join(onnx_dir, f)) / (1024 * 1024)
        print(f"  - {f} ({size_mb:.1f} MB)")

    # Convert each model
    results = {}

    for onnx_file in onnx_files:
        onnx_path = os.path.join(onnx_dir, onnx_file)
        tflite_file = onnx_file.replace('.onnx', '.tflite')
        tflite_path = os.path.join(output_dir, tflite_file)

        success = convert_onnx_to_tflite(onnx_path, tflite_path, temp_dir)
        results[onnx_file] = success

    # Cleanup temp directory
    shutil.rmtree(temp_dir, ignore_errors=True)

    # Summary
    print("\n" + "=" * 60)
    print("CONVERSION SUMMARY")
    print("=" * 60)

    success_count = sum(1 for v in results.values() if v)
    failed_count = sum(1 for v in results.values() if not v)

    print(f"\n✅ Successful: {success_count}")
    print(f"❌ Failed: {failed_count}")

    if failed_count > 0:
        print("\nFailed models:")
        for name, success in results.items():
            if not success:
                print(f"  - {name}")

    print(f"\n📁 Output directory: {output_dir}")
    print("\nNext steps:")
    print("1. Rebuild the Android app")
    print("2. Test with the new TFLite models")

if __name__ == "__main__":
    main()

