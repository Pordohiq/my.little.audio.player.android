# ResTree Documentation
This is a utility class that handles all interactions with the Audio Library.

## Global Variables
- **`library_root`**: The URI of the root directory of the audio library.
- **`library`**: A list of all the elements in the audio library. The "root" element.
- **`current_folder`**: A list of the elements in the current folder. FileView uses this list to display the folder.

## General Methods
- **`init(Context context)`**: Initializes the audio library.
- **`reload_from_disk(Context context)`**: Reloads the audio library from the disk. Essentially performs init(), but expects the library to be initialized.

## Path Config
These are methods for reading and writing the path config. The path config is a file located at `getFilesDir():/library.path`. The file contains the URI of the root directory of the audio library.

- **`read_path_config(Context context)`**: Reads the path config
- **`write_path_config(Context context, Uri newPath)`**: Writes the path config

## Direct ResTree Interactions
These are methods that load, read and modify the audio library structure. They primarily interact with `library` and `current_folder`.
By themselves they don't directly interact with the storage on the phone but rather look through the library structure except the function `load library`.
