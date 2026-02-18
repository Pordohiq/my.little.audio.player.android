# ResTree Documentation
This is a utility class that handles all interactions with the Audio Library.

## Global Variables
- **`library_root`**: The URI of the root directory of the audio library.
- **`library`**: A list of all the elements in the audio library. The "root" element.
- **`current_folder`**: A list of the elements in the current folder. FileView uses this list to display the folder.

## General Methods

## Path Config
These are methods for reading and writing the path config. The path config is a file located at `getFilesDir():/library.path`. The file contains the URI of the root directory of the audio library.

- **`read_path_config(Context context)`**: Reads the path config
- **`write_path_config(Context context, Uri newPath)`**: Writes the path config

## ResTree Interactions
These are methods that load, read and modify the audio library structure. They primarily interact with `library` and `current_folder`.
By themselves they don't directly interact with the storage on the phone but rather look through the library structure except the function `load library`.

- **`load_library(Uri uri)`** This is the function that opens the directory at the given Uri and then creates the Library Structure from `DiskElement` components.
- **`load_folder(List<String> path)`** This function is used for loading a specific folder from the library structure. Typically used to fill out `current_folder`
- **`get_element_at_path(List<String> sd_path)`** This function returns the DiskElement that is found at the specific path. If no DiskElement is found the function returns null.
- **`get_local_element_path(DiskElement element, List<DiskElement> data, List<String> path)`** This returns the `List<String>` internal path of a file in the structure.
- **`get_parent(DiskElement element)`** This function returns the parent of the given element.

## SAF Interactions
These are private methods that are used by the API section and are the ones that actually *do stuff* on the disk. (Quite honestly these are the functions, where I pretty much rely on AI, because I am very much confused, what DocumentUri and TreeUri and another Uri and java Uri, and I am confused.)

- **`SAF_create_folder(Uri parentUri, String folderName)`** This function creates a folder at the given parentUri with the name provided. It returns the Uri of the newly generated Folder.

### SAF_delete_element

- **`SAF_delete_element(DocumentFile file)`** This function deletes a file or folder. The deletion is recursive.
- **`SAF_delete_element(Uri uri)`** This function acts as a wrapper for the upper function and converts the Uri to a DocumentFile.

### SAF_copy_element

These functions are used in moving operations, because I don't know how to implement direct moving of files. 

- **`SAF_copy_element(Context context, DocumentFile source, DocumentFile targetParent)`** This function copies a file or folder. The copy is recursive.
- **`SAF_copy_element(Uri sourceUri, Uri targetParentUri)`**  This essentially acts as a wrapper for upper function, so you can just pass in a Uri instead of a DocumentsProvider

### SAF_rename_element

These functions are used in renaming operations.

- **`SAF_rename_element(DocumentFile file, String newName)`** This function renames a file or folder.
- **`SAF_rename_element(Uri uri, String newName)`** This function acts as a wrapper for the upper function.

### Conversion Helpers

These do not directly interact with the SAF itself, but they are used, because they make things more convenient.

- **`get_DocumentFile_from_Uri`** This function converts a Uri to a DocumentFile. It serves the purpose of handling different Uri types.
- **`get_fileName_from_Uri`** This function converts a Uri to a String. It serves the purpose of handling different Uri types.

## API
These are the functions that are exposed and actively used by other parts of the app. The SAF Region only takes care of the disk in these matters and the functions below also modify the loaded documents structure. The goal of these functions is that you don't need to reload the entire structure every time something changes.

- **`init(Context context)`**: Initializes the audio library.
- **`add_audio_file(Uri audio_uri, List<String> path)`** This function adds an audio file to the library. This function expects a Uri provided by a System File Picker.
- **`create_new_folder(List<String> path, String folder_name)`** This function creates a new folder in the library.
- **`reload_from_disk(Context context)`**: Reloads the audio library from the disk. Essentially performs init(), but expects the library to be initialized.
- **`set_library_path(Uri uri)`** This function sets the library path.
- **`move_file(DiskElement element, List<String> path)`** This function moves a file to a new path. It expects a Uri from a System Folder Picker.
- **`rename_file(DiskElement element, String newName)`** This function renames a file. It takes a DiskElement and a String and Updates the structure.
- **`delete_file(DiskElement element)`** This function deletes a file from the library.

*LomJek, 18.2.2026*