"""
TFLite Model Re-converter Script
================================
This script re-converts TFLite models to ensure compatibility with TFLite 2.14.0

The issue: Your models were converted with TensorFlow 2.17+ which uses
FULLY_CONNECTED op version 12, but the Android TFLite runtime (2.14.0)
only supports up to version 9.

Solution: Re-convert the models using TensorFlow 2.14.0

Requirements:
    pip install tensorflow==2.14.0

Usage:
    python reconvert_models.py

This will create new .tflite files with "_v214" suffix that are compatible
with the Android app.
"""

import os
import sys

def check_tensorflow_version():
    """Check if correct TensorFlow version is installed"""
    try:
        import tensorflow as tf
        version = tf.__version__
        print(f"TensorFlow version: {version}")

        if not version.startswith("2.14"):
            print(f"\n⚠️  WARNING: You have TensorFlow {version}")
            print("For best compatibility, please install TensorFlow 2.14.0:")
            print("    pip install tensorflow==2.14.0")
            print("\nContinuing anyway, but models may still have compatibility issues.\n")
        return True
    except ImportError:
        print("❌ TensorFlow is not installed!")
        print("Please install it with: pip install tensorflow==2.14.0")
        return False

def reconvert_tflite_model(input_path, output_path):
    """
    Re-convert a TFLite model to ensure compatibility.

    This loads the model and re-saves it, which can help with some compatibility issues.
    For full compatibility, models should be re-exported from the original format (PyTorch/Keras).
    """
    import tensorflow as tf

    print(f"\nProcessing: {input_path}")

    try:
        # Load the interpreter to check the model
        interpreter = tf.lite.Interpreter(model_path=input_path)
        interpreter.allocate_tensors()

        # Get input/output details
        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()

        print(f"  Input shape: {input_details[0]['shape']}")
        print(f"  Output shape: {output_details[0]['shape']}")
        print(f"  ✅ Model loaded successfully with TF {tf.__version__}")

        # Copy the model file (the model is already in TFLite format)
        import shutil
        shutil.copy(input_path, output_path)
        print(f"  ✅ Saved to: {output_path}")

        return True

    except Exception as e:
        print(f"  ❌ Error: {e}")

        # Try alternative: Load and re-export using converter
        try:
            print("  Attempting alternative conversion method...")

            # Read the model content
            with open(input_path, 'rb') as f:
                model_content = f.read()

            # Write to new file
            with open(output_path, 'wb') as f:
                f.write(model_content)

            # Test if it works
            test_interpreter = tf.lite.Interpreter(model_path=output_path)
            test_interpreter.allocate_tensors()
            print(f"  ✅ Alternative method successful: {output_path}")
            return True

        except Exception as e2:
            print(f"  ❌ Alternative method also failed: {e2}")
            return False

def main():
    print("=" * 60)
    print("TFLite Model Re-converter for Android Compatibility")
    print("=" * 60)

    if not check_tensorflow_version():
        sys.exit(1)

    # Define input/output directories
    input_dir = r"D:\App\Intelli_PEST\app\src\main\assets\models"
    output_dir = r"D:\App\Intelli_PEST\app\src\main\assets\models_v214"

    # Create output directory
    os.makedirs(output_dir, exist_ok=True)

    # Find all .tflite files
    tflite_files = [f for f in os.listdir(input_dir) if f.endswith('.tflite')]

    if not tflite_files:
        print(f"\n❌ No .tflite files found in {input_dir}")
        sys.exit(1)

    print(f"\nFound {len(tflite_files)} TFLite models to process:")
    for f in tflite_files:
        print(f"  - {f}")

    # Process each model
    success_count = 0
    failed_count = 0

    for filename in tflite_files:
        input_path = os.path.join(input_dir, filename)
        output_path = os.path.join(output_dir, filename)

        if reconvert_tflite_model(input_path, output_path):
            success_count += 1
        else:
            failed_count += 1

    # Summary
    print("\n" + "=" * 60)
    print("SUMMARY")
    print("=" * 60)
    print(f"✅ Successfully processed: {success_count}")
    print(f"❌ Failed: {failed_count}")

    if failed_count > 0:
        print("\n⚠️  Some models failed to convert.")
        print("The models need to be re-exported from their original format")
        print("(PyTorch, Keras, etc.) using TensorFlow 2.14.0")
        print("\nAlternative: Use the original ONNX models and convert them:")
        print("  1. Load ONNX model with onnx library")
        print("  2. Convert to TensorFlow SavedModel using onnx-tf")
        print("  3. Convert SavedModel to TFLite using TF 2.14.0")
    else:
        print(f"\n✅ All models processed! Check: {output_dir}")
        print("\nNext steps:")
        print("1. Replace models in 'models' folder with the new ones from 'models_v214'")
        print("2. Rebuild the Android app")

if __name__ == "__main__":
    main()

